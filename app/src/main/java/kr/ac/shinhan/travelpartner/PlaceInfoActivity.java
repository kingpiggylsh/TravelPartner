package kr.ac.shinhan.travelpartner;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.net.URL;

import kr.ac.shinhan.travelpartner.Item.DetailWithTourItem;
import kr.ac.shinhan.travelpartner.Item.PlaceInfoItem;
import kr.ac.shinhan.travelpartner.Item.PlaceItem;

import static kr.ac.shinhan.travelpartner.XMLparsing.ServiceDefinition.APPNAME;
import static kr.ac.shinhan.travelpartner.XMLparsing.ServiceDefinition.KEY;
import static kr.ac.shinhan.travelpartner.XMLparsing.ServiceDefinition.OS;
import static kr.ac.shinhan.travelpartner.XMLparsing.ServiceDefinition.SERVICE_DETAIL_INTRO;
import static kr.ac.shinhan.travelpartner.XMLparsing.ServiceDefinition.SERVICE_DETAIL_WITH_TOUR;
import static kr.ac.shinhan.travelpartner.XMLparsing.ServiceDefinition.SERVICE_URL;

public class PlaceInfoActivity extends AppCompatActivity {
    private PlaceInfoItem placeInfoItem = new PlaceInfoItem();
    private TextView mContentTypeId, mTitle, mTel, mAddr, mOpenTime, mRestTime;
    private ImageView mStroller, mPet, mParking;
    private String chkbabycarriage, chkpet, restdate, parking, usetime, opentime;
    private ImageView mImage;
    private Button mTelBtn, mAddrBtn, mWriteReviewBtn, mFavoriteBtn;
    private String contentId, image, contentTypeId, uiContentType, title, tel, addr;
    private double lat, lon;
    boolean isParking, isPet, isBabycarriage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_info);

        Intent intent = getIntent();
        contentId = intent.getStringExtra("contentid");
        image = intent.getStringExtra("image");
        contentTypeId = intent.getStringExtra("contentTypeId");
        uiContentType = intent.getStringExtra("uiContentTypeId");
        title = intent.getStringExtra("title");
        tel = intent.getStringExtra("tel");
        addr = intent.getStringExtra("addr");
        lat = intent.getDoubleExtra("latitude", 0);
        lon = intent.getDoubleExtra("longitude", 0);

        initUI();
        new PlaceInfoParsing().execute(contentId, contentTypeId);

    }

    View.OnClickListener btnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent;
            switch (v.getId()) {
                case R.id.btn_info_tel:
                    if (tel != null) {
                        intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + tel));
                        startActivity(intent);
                        break;
                    } else {
                        Toast.makeText(getApplicationContext(), "전화번호 정보가 없습니다.", Toast.LENGTH_SHORT).show();
                        break;
                    }
                case R.id.btn_info_addr:
                    String geoCode = "geo:" + lat + "," + lon + "?q=" + title;
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(geoCode));
                    startActivity(intent);
                    break;
                case R.id.btn_info_write_review:
                    break;
                case R.id.btn_info_favorite:
                    break;
            }
        }
    };

    public void initUI() {
        mImage = (ImageView) findViewById(R.id.iv_info_image);
        mContentTypeId = (TextView) findViewById(R.id.tv_info_contenttypeid);
        mTitle = (TextView) findViewById(R.id.tv_info_title);
        mTel = (TextView) findViewById(R.id.tv_info_tel);
        mAddr = (TextView) findViewById(R.id.tv_info_addr);
        mOpenTime = (TextView) findViewById(R.id.tv_info_opentime);
        mRestTime = (TextView) findViewById(R.id.tv_info_resttime);
        mTelBtn = (Button) findViewById(R.id.btn_info_tel);
        mAddrBtn = (Button) findViewById(R.id.btn_info_addr);

        mTelBtn = (Button) findViewById(R.id.btn_info_tel);
        mAddrBtn = (Button) findViewById(R.id.btn_info_addr);
        mWriteReviewBtn = (Button) findViewById(R.id.btn_info_write_review);
        mFavoriteBtn = (Button) findViewById(R.id.btn_info_favorite);

        mStroller = (ImageView) findViewById(R.id.iv_info_stroller);
        mPet = (ImageView) findViewById(R.id.iv_info_pet);
        mParking = (ImageView) findViewById(R.id.iv_info_parking);

        mTelBtn.setOnClickListener(btnListener);
        mAddrBtn.setOnClickListener(btnListener);
        mWriteReviewBtn.setOnClickListener(btnListener);
        mFavoriteBtn.setOnClickListener(btnListener);

        Picasso.get().load(image).into(mImage);
        mContentTypeId.setText(uiContentType);
        mTitle.setText(title);
        mTel.setText(tel);
        mAddr.setText(addr);
        mOpenTime.setSelected(true);
        mRestTime.setSelected(true);

    }

    class PlaceInfoParsing extends AsyncTask<String, String, PlaceInfoItem> {

        @Override
        protected PlaceInfoItem doInBackground(String... strings) {
            try {
                contentId = strings[0];
                contentTypeId = strings[1];

                URL detailIntroUrl = new URL(SERVICE_URL + SERVICE_DETAIL_INTRO + "ServiceKey=" + KEY + "&MobileOS=" + OS + "&MobileApp=" + APPNAME +
                        "&contentId=" + contentId + "&contentTypeId=" + contentTypeId);
                XmlPullParserFactory parserCreator = XmlPullParserFactory.newInstance();
                XmlPullParser parser = parserCreator.newPullParser();

                parser.setInput(detailIntroUrl.openStream(), "UTF-8");
                int parserEvent = parser.getEventType();

                while (parserEvent != XmlPullParser.END_DOCUMENT) {
                    switch (parserEvent) {
                        case XmlPullParser.START_TAG:
                            String tag = parser.getName();
                            if (tag.equals("item")) {
                                placeInfoItem = new PlaceInfoItem();
                            } else if (tag.contains("chkbabycarriage")) {
                                parser.next();
                                chkbabycarriage = parser.getText();
                                placeInfoItem.setChkbabycarriage(chkbabycarriage);
                            } else if (tag.contains("chkpet")) {
                                parser.next();
                                chkpet = parser.getText();
                                placeInfoItem.setChkpet(chkpet);
                            } else if (tag.contains("opentime")) {
                                parser.next();
                                opentime = parser.getText();
                                opentime = opentime.replace("<br />","");
                                opentime=opentime.replace("<br/>","");
                                opentime=opentime.replace("<br>","");
                                placeInfoItem.setOpentime(opentime);
                            } else if (tag.equals("parking") || tag.equals("parkingculture") || tag.equals("parkingleports")
                                    || tag.equals("parkinglodging") || tag.equals("parkingshopping") || tag.equals("parkingfood")) {
                                parser.next();
                                parking = parser.getText();
                                placeInfoItem.setParking(parking);
                            } else if (tag.contains("restdate")) {
                                parser.next();
                                restdate = parser.getText();
                                restdate = restdate.replace("<br />","");
                                restdate = restdate.replace("<br/>","");
                                restdate = restdate.replace("<br>","");
                                placeInfoItem.setRestdate(restdate);
                            } else if (tag.contains("usetime")) {
                                parser.next();
                                usetime = parser.getText();
                                usetime = usetime.replace("<br />","");
                                usetime = usetime.replace("<br/>","");
                                usetime = usetime.replace("<br>","");
                                placeInfoItem.setUsetime(usetime);
                            }
                            break;
                    }
                    parserEvent = parser.next();
                }
            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
            }
            return placeInfoItem;
        }

        @Override
        protected void onPostExecute(PlaceInfoItem placeInfoItem) {
            super.onPostExecute(placeInfoItem);
            checkBooleans(placeInfoItem);
            setUI();
        }

        public void checkBooleans(PlaceInfoItem placeInfoItem) {
            if (placeInfoItem.getChkbabycarriage() != null) {
                if (placeInfoItem.getChkbabycarriage().contains("없음") | placeInfoItem.getChkbabycarriage().contains("불가")) {
                    isBabycarriage = false;
                } else if (placeInfoItem.getChkbabycarriage().contains("있음") | placeInfoItem.getChkbabycarriage().contains("가능")) {
                    isBabycarriage = true;
                } else {
                    mStroller.setVisibility(View.INVISIBLE);
                }
            }
            if (placeInfoItem.getChkpet() != null) {
                if (placeInfoItem.getChkpet().contains("없음") | placeInfoItem.getChkpet().contains("불가")) {
                    isPet = false;
                } else if (placeInfoItem.getChkpet().contains("있음") | placeInfoItem.getChkpet().contains("가능")) {
                    isPet = true;
                } else {
                    mPet.setVisibility(View.INVISIBLE);
                }
            }
            if (placeInfoItem.getParking() != null) {
                if (placeInfoItem.getParking().contains("없음") | placeInfoItem.getParking().contains("불가")) {
                    isParking = false;
                } else if (placeInfoItem.getParking().contains("있음") | placeInfoItem.getParking().contains("가능")) {
                    isParking = true;
                } else {
                    mParking.setVisibility(View.INVISIBLE);
                }
            }
        }

        public void setUI() {

            if (isBabycarriage) {
                mStroller.setImageResource(R.drawable.ic_stroller);
            } else {
                mStroller.setImageResource(R.drawable.ic_stroller_gray);

            }
            if (isPet) {
                mPet.setImageResource(R.drawable.ic_pet);
            } else {
                mPet.setImageResource(R.drawable.ic_pet_gray);
            }
            if (isParking) {
                mParking.setImageResource(R.drawable.ic_parking);
            } else {
                mParking.setImageResource(R.drawable.ic_parking_gray);
            }
            if (placeInfoItem.getOpentime() != null) {
                mOpenTime.setText(placeInfoItem.getOpentime());
            }
            if (placeInfoItem.getUsetime() != null) {
                mOpenTime.setText(placeInfoItem.getUsetime());
            }
            if (placeInfoItem.getRestdate() != null) {
                mRestTime.setText(placeInfoItem.getRestdate());
            }
        }
    }

    class DetailWithTourParsing extends AsyncTask<String, String, DetailWithTourItem> {

        @Override
        protected DetailWithTourItem doInBackground(String... strings) {
            DetailWithTourItem detailWithTourItem = null;
            try {
                String contentId = strings[0];
                String contentTypeId = strings[1];

                URL detailIntroUrl = new URL(SERVICE_URL + SERVICE_DETAIL_WITH_TOUR + "ServiceKey=" + KEY + "&MobileOS=" + OS + "&MobileApp=" + APPNAME +
                        "&contentId=" + contentId);
                XmlPullParserFactory parserCreator = XmlPullParserFactory.newInstance();
                XmlPullParser parser = parserCreator.newPullParser();

                parser.setInput(detailIntroUrl.openStream(), "UTF-8");
                int parserEvent = parser.getEventType();

                while (parserEvent != XmlPullParser.END_DOCUMENT) {
                    switch (parserEvent) {
                        case XmlPullParser.START_TAG:
                            String tag = parser.getName();
                            if (tag.contains("item")) {
                                detailWithTourItem = new DetailWithTourItem();
                            } else if (tag.equals("chkbabycarriage")) {
                                parser.next();
                                //chkbabycarriage = parser.getText();
                                //placeInfoItem.setChkbabycarriage(chkbabycarriage);
                            }
                            break;
                    }
                    parserEvent = parser.next();
                }
            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
            }
            return detailWithTourItem;
        }

        @Override
        protected void onPostExecute(DetailWithTourItem detailWithTourItem) {
            super.onPostExecute(detailWithTourItem);

        }

    }

}