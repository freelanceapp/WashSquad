package com.creative.share.apps.wash_squad_driver.activities_fragments.activity_service_details;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.creative.share.apps.wash_squad_driver.R;
import com.creative.share.apps.wash_squad_driver.activities_fragments.activity_map.MapActivity;
import com.creative.share.apps.wash_squad_driver.activities_fragments.activity_payment.PaymentActivity;
import com.creative.share.apps.wash_squad_driver.activities_fragments.activity_time.TimeActivity;
import com.creative.share.apps.wash_squad_driver.activities_fragments.calendar_activity.CalendarActivity;
import com.creative.share.apps.wash_squad_driver.adapters.AdditionalServiceAdapter;
import com.creative.share.apps.wash_squad_driver.adapters.CarSizeAdapter;
import com.creative.share.apps.wash_squad_driver.adapters.CarTypeAdapter;
import com.creative.share.apps.wash_squad_driver.databinding.ActivityServiceDetailsBinding;
import com.creative.share.apps.wash_squad_driver.interfaces.Listeners;
import com.creative.share.apps.wash_squad_driver.language.LanguageHelper;
import com.creative.share.apps.wash_squad_driver.models.CarSizeDataModel;
import com.creative.share.apps.wash_squad_driver.models.CarTypeDataModel;
import com.creative.share.apps.wash_squad_driver.models.ItemToUpload;
import com.creative.share.apps.wash_squad_driver.models.SelectedLocation;
import com.creative.share.apps.wash_squad_driver.models.ServiceDataModel;
import com.creative.share.apps.wash_squad_driver.models.TimeDataModel;
import com.creative.share.apps.wash_squad_driver.models.UserModel;
import com.creative.share.apps.wash_squad_driver.preferences.Preferences;
import com.creative.share.apps.wash_squad_driver.remote.Api;
import com.creative.share.apps.wash_squad_driver.share.Common;
import com.creative.share.apps.wash_squad_driver.tags.Tags;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServiceDetailsActivity extends AppCompatActivity implements Listeners.BackListener {
    private ActivityServiceDetailsBinding binding;
    private String lang;
    private SelectedLocation selectedLocation;
    private ServiceDataModel.Level2 serviceModel;
    private long date=0;
    private List<CarSizeDataModel.CarSizeModel> carSizeModelList;
    private List<CarTypeDataModel.CarTypeModel> carTypeModelList;
    private AdditionalServiceAdapter additionalServiceAdapter;
    private CarSizeAdapter carSizeAdapter;
    private CarTypeAdapter carTypeAdapter;
    private GridLayoutManager manager1;
    private LinearLayoutManager manager2;
    private TimeDataModel.TimeModel timeModel;
    private String d;
    private List<ServiceDataModel.Level3> additional_service;
    private ItemToUpload itemToUpload;
    private int service_id;
    private String service_name_ar,service_name_en;
    private UserModel userModel;
    private Preferences preferences;

    @Override
    protected void attachBaseContext(Context newBase) {
        Paper.init(newBase);
        super.attachBaseContext(LanguageHelper.updateResources(newBase, Paper.book().read("lang", Locale.getDefault().getLanguage())));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_service_details);
        getDataFromIntent();
        initView();


    }

    private void getDataFromIntent() {
        Intent intent = getIntent();
        if (intent!=null)
        {
            serviceModel = (ServiceDataModel.Level2) intent.getSerializableExtra("data");
            service_id = intent.getIntExtra("service_id",0);
            service_name_ar = intent.getStringExtra("service_name_ar");
            service_name_en = intent.getStringExtra("service_name_en");

        }
    }


    private void initView() {
        preferences = Preferences.newInstance();
        userModel = preferences.getUserData(this);
        itemToUpload = new ItemToUpload();
        itemToUpload.setService_id(service_id);
        itemToUpload.setAr_service_type(service_name_ar);
        itemToUpload.setEn_service_type(service_name_en);
        binding.setItemModel(itemToUpload);
        additional_service = new ArrayList<>();
        carSizeModelList = new ArrayList<>();
        carTypeModelList = new ArrayList<>();
        carTypeModelList.add(new CarTypeDataModel.CarTypeModel("نوع السيارة","Car type"));
        Paper.init(this);
        lang = Paper.book().read("lang", Locale.getDefault().getLanguage());
        binding.setLang(lang);
        binding.setBackListener(this);
        binding.setLevel2(serviceModel);
        if (lang.equals("ar"))
        {
            if (serviceModel.getAr_des()!=null && !TextUtils.isEmpty(serviceModel.getAr_des()))
            {
                binding.tvDetails.setVisibility(View.VISIBLE);
            }else
                {
                    binding.tvDetails.setVisibility(View.GONE);

                }
        }else
            {
                if (serviceModel.getEn_des()!=null && !TextUtils.isEmpty(serviceModel.getEn_des()))
                {
                    binding.tvDetails.setVisibility(View.VISIBLE);
                }else
                {
                    binding.tvDetails.setVisibility(View.GONE);

                }
            }


        manager1 = new GridLayoutManager(this,2);
        carSizeAdapter = new CarSizeAdapter(carSizeModelList,this);
        binding.recView.setLayoutManager(manager1);
        binding.recView.setAdapter(carSizeAdapter);
        carTypeAdapter = new CarTypeAdapter(this,carTypeModelList);
        binding.spinner.setAdapter(carTypeAdapter);

        binding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                if (i==0)
                {
                    itemToUpload.setCarType_id(0);
                    itemToUpload.setAr_car_type("");
                    itemToUpload.setEn_car_type("");

                    binding.setItemModel(itemToUpload);
                }else
                    {
                        itemToUpload.setCarType_id(carTypeModelList.get(i).getId());
                        itemToUpload.setAr_car_type(carTypeModelList.get(i).getAr_title());
                        itemToUpload.setEn_car_type(carTypeModelList.get(i).getEn_title());

                        binding.setItemModel(itemToUpload);
                    }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        manager2 = new LinearLayoutManager(this);
        binding.recViewService.setLayoutManager(manager2);

        if (serviceModel.getLevel3().size()>0)
        {
            binding.llAdditional.setVisibility(View.VISIBLE);
            additionalServiceAdapter = new AdditionalServiceAdapter(serviceModel.getLevel3(),this);
            binding.recViewService.setAdapter(additionalServiceAdapter);

        }else
            {
                binding.llAdditional.setVisibility(View.GONE);
            }


        binding.consMap.setOnClickListener(view -> {
            Intent intent = new Intent(ServiceDetailsActivity.this, MapActivity.class);
            startActivityForResult(intent,1);
        });
        binding.consDate.setOnClickListener(view -> {

            Intent intent = new Intent(this, CalendarActivity.class);
            startActivityForResult(intent,2);



        });
        binding.consTime.setOnClickListener(view -> {
            if (date!=0)
            {
                Intent intent = new Intent(ServiceDetailsActivity.this, TimeActivity.class);
                intent.putExtra("date",d);
                startActivityForResult(intent,3);
            }else
            {

                date = Calendar.getInstance().getTimeInMillis();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd",Locale.ENGLISH);
                d = dateFormat.format(new Date(date));
                binding.tvDate.setText(d);

                itemToUpload.setOrder_date(date);
                binding.setItemModel(itemToUpload);

                Intent intent = new Intent(ServiceDetailsActivity.this, TimeActivity.class);
                startActivityForResult(intent,3);
            }

        });
        binding.tvDetails.setOnClickListener(view -> {
            if (binding.expandLayout.isExpanded())
            {
                binding.expandLayout.collapse(true);
                binding.tvDetails.setText(getString(R.string.more_details));
            }else
            {
                binding.expandLayout.setExpanded(true,true);
                binding.tvDetails.setText(getString(R.string.less_details));

            }
        });

        binding.btnSendOrder.setOnClickListener(view -> {
            if (itemToUpload.isDataValidStep1(this))
            {
                if (userModel!=null)
                {
                    itemToUpload.setUser_id(userModel.getId());
                    itemToUpload.setUser_name(userModel.getFull_name());
                    itemToUpload.setUser_phone(userModel.getPhone());

                    Intent intent = new Intent(this, PaymentActivity.class);
                    intent.putExtra("item",itemToUpload);
                    startActivityForResult(intent,4);
                }else
                {
                    Common.CreateNoSignAlertDialog(this);
                }



            }
        });

        getCarSize();
        getCarType();





    }


    private void getCarSize() {

        ProgressDialog dialog = Common.createProgressDialog(ServiceDetailsActivity.this,getString(R.string.wait));
        dialog.setCancelable(false);
        dialog.show();

        Api.getService(Tags.base_url)
                .getCarSizes()
                .enqueue(new Callback<CarSizeDataModel>() {
                    @Override
                    public void onResponse(Call<CarSizeDataModel> call, Response<CarSizeDataModel> response) {
                        dialog.dismiss();
                        if (response.isSuccessful()&&response.body()!=null)
                        {
                            carSizeModelList.clear();
                            carSizeModelList.addAll(response.body().getData());
                            carSizeAdapter.notifyDataSetChanged();

                        }else
                        {
                            try {

                                Log.e("error",response.code()+"_"+response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (response.code()==404)
                            {
                            }else if (response.code() == 500) {
                                Toast.makeText(ServiceDetailsActivity.this, "Server Error", Toast.LENGTH_SHORT).show();

                            }else
                            {
                                Toast.makeText(ServiceDetailsActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();


                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<CarSizeDataModel> call, Throwable t) {
                        try {
                            dialog.dismiss();
                            if (t.getMessage()!=null)
                            {
                                Log.e("error",t.getMessage());
                                if (t.getMessage().toLowerCase().contains("failed to connect")||t.getMessage().toLowerCase().contains("unable to resolve host"))
                                {
                                    Toast.makeText(ServiceDetailsActivity.this,R.string.something, Toast.LENGTH_SHORT).show();
                                }else
                                {
                                    Toast.makeText(ServiceDetailsActivity.this,t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }

                        }catch (Exception e){}
                    }
                });
    }

    private void getCarType() {
        ProgressDialog dialog = Common.createProgressDialog(ServiceDetailsActivity.this,getString(R.string.wait));
        dialog.setCancelable(false);
        dialog.show();

        Api.getService(Tags.base_url)
                .getCarTypes()
                .enqueue(new Callback<CarTypeDataModel>() {
                    @Override
                    public void onResponse(Call<CarTypeDataModel> call, Response<CarTypeDataModel> response) {
                        dialog.dismiss();
                        if (response.isSuccessful()&&response.body()!=null)
                        {
                            carTypeModelList.addAll(response.body().getData());
                            carTypeAdapter.notifyDataSetChanged();

                        }else
                        {
                            try {

                                Log.e("error",response.code()+"_"+response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (response.code()==404)
                            {
                            }else if (response.code() == 500) {
                                Toast.makeText(ServiceDetailsActivity.this, "Server Error", Toast.LENGTH_SHORT).show();

                            }else
                            {
                                Toast.makeText(ServiceDetailsActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();


                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<CarTypeDataModel> call, Throwable t) {
                        try {
                            dialog.dismiss();
                            if (t.getMessage()!=null)
                            {
                                Log.e("error",t.getMessage());
                                if (t.getMessage().toLowerCase().contains("failed to connect")||t.getMessage().toLowerCase().contains("unable to resolve host"))
                                {
                                    Toast.makeText(ServiceDetailsActivity.this,R.string.something, Toast.LENGTH_SHORT).show();
                                }else
                                {
                                    Toast.makeText(ServiceDetailsActivity.this,t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }

                        }catch (Exception e){}
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==1&&resultCode==RESULT_OK&&data!=null)
        {
            if (data.hasExtra("location"))
            {
                selectedLocation = (SelectedLocation) data.getSerializableExtra("location");
                binding.setLocation(selectedLocation);
                itemToUpload.setLatitude(String.valueOf(selectedLocation.getLat()));
                itemToUpload.setLongitude(String.valueOf(selectedLocation.getLng()));
                itemToUpload.setAddress(selectedLocation.getAddress());
                binding.setItemModel(itemToUpload);

            }
        }else if (requestCode==2&&resultCode==RESULT_OK&&data!=null)
        {
            if (data.hasExtra("date"))
            {
                binding.tvTime.setText("");
                timeModel=null;
                date = data.getLongExtra("date",0);
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd",Locale.ENGLISH);
                d = dateFormat.format(new Date(date));
                binding.tvDate.setText(d);
                itemToUpload.setOrder_date(date);
                binding.setItemModel(itemToUpload);


            }
        }
        else if (requestCode==3&&resultCode==RESULT_OK&&data!=null)
        {
            if (data.hasExtra("data"))
            {
                timeModel = (TimeDataModel.TimeModel) data.getSerializableExtra("data");
                String am_pm = timeModel.getType().equals("1")?getString(R.string.am):getString(R.string.pm);
                String time = timeModel.getTime_text();
                binding.tvTime.setText(String.format("%s %s",time,am_pm));

                itemToUpload.setOrder_time_id(timeModel.getId());
                itemToUpload.setTime(time);
                itemToUpload.setTime_type(am_pm);
                binding.setItemModel(itemToUpload);
            }
        }

        else if (requestCode==4&&resultCode==RESULT_OK&&data!=null)
        {
            Intent intent = getIntent();
            if (intent!=null){

                setResult(RESULT_OK,intent);

            }
            finish();
        }
    }

    public void setItemCarSizeSelected(CarSizeDataModel.CarSizeModel carSizeModel) {
        itemToUpload.setCarSize_id(carSizeModel.getId());
        itemToUpload.setService_price(Double.parseDouble(carSizeModel.getPrice()));
        binding.setItemModel(itemToUpload);
    }


    public void setItemAdditionService(ServiceDataModel.Level3 serviceModel)
    {

        if (!hasItem(serviceModel))
        {
            additional_service.add(serviceModel);

            List<ItemToUpload.SubServiceModel> subServiceModelList = new ArrayList<>();
            for (ServiceDataModel.Level3 level3:additional_service)
            {
                ItemToUpload.SubServiceModel subServiceModel = new ItemToUpload.SubServiceModel(level3.getId(),Double.parseDouble(level3.getPrice()),level3.getAr_title(),level3.getEn_title());
                subServiceModelList.add(subServiceModel);


            }
            Log.e("size",subServiceModelList.size()+"_");

            itemToUpload.setSub_services(subServiceModelList);
        }
    }

    public void removeAdditionalItem(ServiceDataModel.Level3 m_level3) {
        additional_service.remove(getItemPos(m_level3));

        List<ItemToUpload.SubServiceModel> subServiceModelList = new ArrayList<>();
        for (ServiceDataModel.Level3 level3:additional_service)
        {
            ItemToUpload.SubServiceModel subServiceModel = new ItemToUpload.SubServiceModel(level3.getId(),Double.parseDouble(level3.getPrice()),level3.getAr_title(),level3.getEn_title());
            subServiceModelList.add(subServiceModel);

        }
        itemToUpload.setSub_services(subServiceModelList);
    }
    private boolean hasItem(ServiceDataModel.Level3 serviceModel)
    {
        for (ServiceDataModel.Level3 serviceModel2:additional_service)
        {
            if (serviceModel.getId()==serviceModel2.getId())
            {
                return true;
            }
        }
        return false;
    }

    private int getItemPos(ServiceDataModel.Level3 level3)
    {
        for (int i =0;i<additional_service.size();i++)
        {
            if (level3.getId()==additional_service.get(i).getId())
            {
                return i;
            }
        }

        return 0;
    }
    @Override
    public void back() {
        finish();
    }



}
