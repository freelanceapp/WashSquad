package com.creative.share.apps.wash_squad_driver.activities_fragments.activity_home.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.creative.share.apps.wash_squad_driver.R;
import com.creative.share.apps.wash_squad_driver.activities_fragments.activity_home.activity.HomeActivity;
import com.creative.share.apps.wash_squad_driver.databinding.DialogSelectImageBinding;
import com.creative.share.apps.wash_squad_driver.databinding.FragmentProfileBinding;
import com.creative.share.apps.wash_squad_driver.interfaces.Listeners;
import com.creative.share.apps.wash_squad_driver.models.EditProfileModel;
import com.creative.share.apps.wash_squad_driver.models.UserModel;
import com.creative.share.apps.wash_squad_driver.preferences.Preferences;
import com.creative.share.apps.wash_squad_driver.remote.Api;
import com.creative.share.apps.wash_squad_driver.share.Common;
import com.creative.share.apps.wash_squad_driver.tags.Tags;
import com.mukesh.countrypicker.Country;
import com.mukesh.countrypicker.CountryPicker;
import com.mukesh.countrypicker.listeners.OnCountryPickerListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;

import io.paperdb.Paper;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Fragment_Profile extends Fragment implements Listeners.ShowCountryDialogListener, OnCountryPickerListener,Listeners.EditProfileListener {

    private HomeActivity activity;
    private FragmentProfileBinding binding;
    private Preferences preferences;
    private UserModel userModel;
    private CountryPicker countryPicker;
    private String lang;
    private String code;
private EditProfileModel edit_profile_model;
    private final String READ_PERM = Manifest.permission.READ_EXTERNAL_STORAGE;
    private final String write_permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private final String camera_permission = Manifest.permission.CAMERA;
    private final int IMG_REQ1 = 1, IMG_REQ2 = 2;
    private Uri imgUri1 = null;
    public static Fragment_Profile newInstance() {

        return new Fragment_Profile();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false);
        initView();
        return binding.getRoot();
    }

    private void initView() {
        Paper.init(activity);
        lang = Paper.book().read("lang", Locale.getDefault().getLanguage());
        binding.setLang(lang);
        binding.setShowCountryListener(this);

        activity = (HomeActivity) getActivity();
        preferences = Preferences.newInstance();

        userModel = preferences.getUserData(activity);
        if(userModel!=null){
        edit_profile_model=new EditProfileModel(userModel.getFull_name(),userModel.getPhone_code(),userModel.getPhone());}
        else {
            edit_profile_model=new EditProfileModel();
        }
        binding.setEditProfileModel(edit_profile_model);
        binding.edtName.setText(userModel.getFull_name());
        binding.tvCode.setText(userModel.getPhone_code().replaceFirst("00", "+"));
        binding.edtPhone.setText(userModel.getPhone());
        binding.setEditprofilelistener(this);
        code = userModel.getPhone_code();

        createCountryDialog();
binding.image.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        CreateImageAlertDialog();
    }
});
    }
    private void CreateImageAlertDialog()
    {

        final AlertDialog dialog = new AlertDialog.Builder(activity)
                .setCancelable(true)
                .create();

        DialogSelectImageBinding binding = DataBindingUtil.inflate(LayoutInflater.from(activity),R.layout.dialog_select_image,null,false);



        binding.btnCamera.setOnClickListener(v -> {
            dialog.dismiss();
            Check_CameraPermission();

        });

        binding.btnGallery.setOnClickListener(v -> {
            dialog.dismiss();
            CheckReadPermission();



        });

        binding.btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.getWindow().getAttributes().windowAnimations= R.style.dialog_congratulation_animation;
        dialog.setCanceledOnTouchOutside(false);
        dialog.setView(binding.getRoot());
        dialog.show();
    }
    private void CheckReadPermission()
    {
        if (ActivityCompat.checkSelfPermission(activity, READ_PERM) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{READ_PERM}, IMG_REQ1);
        } else {
            SelectImage(IMG_REQ1);
        }
    }

    private void Check_CameraPermission()
    {
        if (ContextCompat.checkSelfPermission(activity,camera_permission)!= PackageManager.PERMISSION_GRANTED&&ContextCompat.checkSelfPermission(activity,write_permission)!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(activity,new String[]{camera_permission,write_permission},IMG_REQ2);
        }else
        {
            SelectImage(IMG_REQ2);

        }

    }
    private void SelectImage(int img_req) {

        Intent intent = new Intent();

        if (img_req == IMG_REQ1)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            {
                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            }else
            {
                intent.setAction(Intent.ACTION_GET_CONTENT);

            }

            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setType("image/*");
            startActivityForResult(intent,img_req);

        }else if (img_req ==IMG_REQ2)
        {
            try {
                intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,img_req);
            }catch (SecurityException e)
            {
                Toast.makeText(activity,R.string.perm_image_denied, Toast.LENGTH_SHORT).show();
            }
            catch (Exception e)
            {
                Toast.makeText(activity,R.string.perm_image_denied, Toast.LENGTH_SHORT).show();

            }


        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == IMG_REQ1) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                SelectImage(IMG_REQ1);
            } else {
                Toast.makeText(activity, getString(R.string.perm_image_denied), Toast.LENGTH_SHORT).show();
            }

        } else if (requestCode == IMG_REQ2) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                SelectImage(IMG_REQ2);
            } else {
                Toast.makeText(activity, getString(R.string.perm_image_denied), Toast.LENGTH_SHORT).show();
            }
        }


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMG_REQ2 && resultCode == Activity.RESULT_OK && data != null) {

            Bitmap bitmap = (Bitmap) data.getExtras().get("data");

            imgUri1 = getUriFromBitmap(bitmap);

           // edit_profile_model.editImageProfile(userModel.getUser().getId(),imgUri1.toString());





        } else if (requestCode == IMG_REQ1 && resultCode == Activity.RESULT_OK && data != null) {

            imgUri1 = data.getData();
          //  edit_profile_view_model.editImageProfile(userModel.getUser().getId(),imgUri1.toString());



        }

    }
    public void editImageProfile(int user_id,String full_name,String image)
    {
        ProgressDialog dialog = Common.createProgressDialog(activity,activity.getString(R.string.wait));
        dialog.setCancelable(false);
        dialog.show();

        RequestBody id_part = Common.getRequestBodyText(String.valueOf(user_id));
        RequestBody name_part = Common.getRequestBodyText(String.valueOf(full_name));

        MultipartBody.Part image_part = Common. getMultiPart(activity, Uri.parse(image),"logo");

        Api.getService(Tags.base_url)
                .editUserImage(id_part,name_part,image_part)
                .enqueue(new Callback<UserModel>() {
                    @Override
                    public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                        dialog.dismiss();
                        if (response.isSuccessful()&&response.body()!=null)
                        {
                            //listener.onSuccess(response.body());
                            edit_profile_model=new EditProfileModel(response.body().getFull_name(),response.body().getPhone_code(),response.body().getPhone());
                            binding.setEditProfileModel(edit_profile_model);
                            Toast.makeText(activity, getString(R.string.suc), Toast.LENGTH_SHORT).show();


                        }else
                        {
                            Log.e("codeimage",response.code()+"_");
                            try {
                                Toast.makeText(activity, getString(R.string.failed), Toast.LENGTH_SHORT).show();

                                Log.e("respons",response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                           // listener.onFailed(response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<UserModel> call, Throwable t) {
                        try {
                            dialog.dismiss();
                            Toast.makeText(activity, getString(R.string.something), Toast.LENGTH_SHORT).show();

                        }catch (Exception e){}
                    }
                });
    }

    private Uri getUriFromBitmap(Bitmap bitmap) {
        String path = "";
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            path = MediaStore.Images.Media.insertImage(activity.getContentResolver(), bitmap, "title", null);
            return Uri.parse(path);

        } catch (SecurityException e) {
            Toast.makeText(activity, getString(R.string.perm_image_denied), Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(activity, getString(R.string.perm_image_denied), Toast.LENGTH_SHORT).show();

        }
        return null;
    }
    private void createCountryDialog() {
        countryPicker = new CountryPicker.Builder()
                .canSearch(true)
                .listener(this)
                .theme(CountryPicker.THEME_NEW)
                .with(activity)
                .build();


        code = userModel.getPhone_code();


    }

    @Override
    public void showDialog() {

        countryPicker.showDialog(activity);
    }

    @Override
    public void onSelectCountry(Country country) {

        updatePhoneCode(country);
    }

    private void updatePhoneCode(Country country) {
        code = country.getDialCode();
        binding.tvCode.setText(country.getDialCode());

    }

    @Override
    public void checkDataEditProfile(String name, String phone_code, String phone) {
        if (phone.startsWith("0"))
        {
            phone = phone.replaceFirst("0","");
        }
        edit_profile_model = new EditProfileModel(name,phone_code,phone);
        binding.setEditprofilelistener(this);

        if (edit_profile_model.isDataValid(activity))
        {
            Editprofile(name,phone_code,phone);
        }
    }
    private void Editprofile(String name, String phone_code, String phone)
    {
        ProgressDialog dialog = Common.createProgressDialog(activity,getString(R.string.wait));
        dialog.setCancelable(false);
        dialog.show();
        try {

            Api.getService(Tags.base_url)
                    .edit_profile(userModel.getId()+"",name,phone_code,phone)
                    .enqueue(new Callback<UserModel>() {
                        @Override
                        public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                            dialog.dismiss();
                            if (response.isSuccessful()&&response.body()!=null)
                            {
                                preferences.create_update_userData(activity,response.body());
                                Toast.makeText(activity, getString(R.string.suc), Toast.LENGTH_SHORT).show();


                            }else
                            {
                                try {

                                    Log.e("error",response.code()+"_"+response.errorBody().string());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                if (response.code() == 422) {
                                    Toast.makeText(activity, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                } else if (response.code()==403)
                                {
                                    Toast.makeText(activity, R.string.user_not_active, Toast.LENGTH_SHORT).show();

                                }else if (response.code()==404)
                                {
                                    Toast.makeText(activity, R.string.inc_phone_pas, Toast.LENGTH_SHORT).show();

                                }else if (response.code()==405)
                                {
                                    Toast.makeText(activity, R.string.not_active_phone, Toast.LENGTH_SHORT).show();

                                }else if (response.code() == 500) {
                                    Toast.makeText(activity, "Server Error", Toast.LENGTH_SHORT).show();

                                }else
                                {
                                    Toast.makeText(activity, getString(R.string.failed), Toast.LENGTH_SHORT).show();


                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<UserModel> call, Throwable t) {
                            try {
                                dialog.dismiss();
                                if (t.getMessage()!=null)
                                {
                                    Log.e("error",t.getMessage());
                                    if (t.getMessage().toLowerCase().contains("failed to connect")||t.getMessage().toLowerCase().contains("unable to resolve host"))
                                    {
                                        Toast.makeText(activity,R.string.something, Toast.LENGTH_SHORT).show();
                                    }else
                                    {
                                        Toast.makeText(activity,t.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }

                            }catch (Exception e){}
                        }
                    });
        }catch (Exception e){
            dialog.dismiss();

        }
    }
}