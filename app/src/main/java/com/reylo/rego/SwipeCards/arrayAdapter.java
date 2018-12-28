package com.reylo.rego.SwipeCards;


import android.content.Context;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.makeramen.roundedimageview.RoundedImageView;
import com.reylo.rego.R;

import java.util.List;

public class arrayAdapter extends ArrayAdapter<cards> {

    private Context context;

    // v stands for view
    // these are used to keep track of which photos to show and hide on card click
    private boolean vFirst = true;
    private boolean vSecond = false;
    private boolean vThird = false;
    private boolean vFourth = false;
    private boolean vFifth = false;
    private boolean vSixth = false;

    // h stands for hide
    // these are used to keep track of which photos to show and hide on card click
    private boolean hFirst = false;
    private boolean hSecond = false;
    private boolean hThird = false;
    private boolean hFourth = false;
    private boolean hFifth = false;
    private boolean hSixth = false;

    public arrayAdapter(Context context, int resourceId, List<cards> items){

        super(context, resourceId, items);

    }

    public View getView(int position, View convertView, ViewGroup parent){

        final cards card_item = getItem(position);

        if (convertView == null){

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item, parent, false);

        }

        final CardView cardView = (CardView) convertView.findViewById(R.id.cardView);
        final TextView name = (TextView) convertView.findViewById(R.id.cardFirstNameAndAge);
        final TextView schoolOrJob = (TextView) convertView.findViewById(R.id.cardSchoolOrJob);
        final RoundedImageView imageOne = (RoundedImageView) convertView.findViewById(R.id.cardUserPicOne);
        final RoundedImageView imageTwo = (RoundedImageView) convertView.findViewById(R.id.cardUserPicTwo);
        final RoundedImageView imageThree = (RoundedImageView) convertView.findViewById(R.id.cardUserPicThree);
        final RoundedImageView imageFour = (RoundedImageView) convertView.findViewById(R.id.cardUserPicFour);
        final RoundedImageView imageFive = (RoundedImageView) convertView.findViewById(R.id.cardUserPicFive);
        final RoundedImageView imageSix = (RoundedImageView) convertView.findViewById(R.id.cardUserPicSix);
        final FrameLayout nextImage = (FrameLayout) convertView.findViewById(R.id.cardImageToRight);
        final FrameLayout prevImage = (FrameLayout) convertView.findViewById(R.id.cardImageToLeft);
        final FrameLayout cardMoreInfo = (FrameLayout) convertView.findViewById(R.id.cardImageBottomMoreInfo);

        cardView.setVisibility(View.GONE);

        String nameAndAge = (card_item.getFirstName() + ", " + Integer.toString(card_item.getAge()));

        if (!card_item.getSchool().equals("default") && !card_item.getJobTitle().equals("default")
                && !card_item.getSchool().equals("") && !card_item.getJobTitle().equals("")) {

            String sorj = (card_item.getSchool() + ", " + card_item.getJobTitle());
            schoolOrJob.setText(sorj);
            schoolOrJob.setVisibility(View.VISIBLE);

        } else if (!card_item.getSchool().equals("default") && !card_item.getSchool().equals("")
                && (card_item.getJobTitle().equals("default") || card_item.getJobTitle().equals(""))) {

            schoolOrJob.setText(card_item.getSchool());
            schoolOrJob.setVisibility(View.VISIBLE);

        } else if ((card_item.getSchool().equals("default") || card_item.getSchool().equals("default"))
                && !card_item.getJobTitle().equals("default") && !card_item.getJobTitle().equals("")) {

            schoolOrJob.setText(card_item.getJobTitle());
            schoolOrJob.setVisibility(View.VISIBLE);

        } else if ((card_item.getSchool().equals("default") || card_item.getSchool().equals(""))
                && (card_item.getJobTitle().equals("default") || card_item.getJobTitle().equals(""))) {

            String sorj = "";
            schoolOrJob.setText(sorj);
            schoolOrJob.setVisibility(View.INVISIBLE);

        }

        name.setText(nameAndAge);

        switch(card_item.getSixPic()){
            case "default":
                hSixth = true;
                break;
            default:
                Glide.clear(imageSix);
                Glide.with(convertView.getContext()).load(card_item.getSixPic()).into(imageSix);
                hSixth = false;
                break;
        }

        switch(card_item.getFivePic()){
            case "default":
                hFifth = true;
                break;
            default:
                Glide.clear(imageFive);
                Glide.with(convertView.getContext()).load(card_item.getFivePic()).into(imageFive);
                hFifth = false;
                break;
        }

        switch(card_item.getFourPic()){
            case "default":
                hFourth = true;
                break;
            default:
                Glide.clear(imageFour);
                Glide.with(convertView.getContext()).load(card_item.getFourPic()).into(imageFour);
                hFourth = false;
                break;
        }

        switch(card_item.getThreePic()){
            case "default":
                hThird = true;
                break;
            default:
                Glide.clear(imageThree);
                Glide.with(convertView.getContext()).load(card_item.getThreePic()).into(imageThree);
                hThird = false;
                break;
        }

        switch(card_item.getTwoPic()){
            case "default":
                hSecond = true;
                break;
            default:
                Glide.clear(imageTwo);
                Glide.with(convertView.getContext()).load(card_item.getTwoPic()).into(imageTwo);
                hSecond = false;
                break;
        }

        switch(card_item.getOnePic()){
            case "default":
                Glide.with(convertView.getContext()).load(R.mipmap.ic_launcher).into(imageOne);
                break;
            default:
                Glide.clear(imageOne);
                Glide.with(convertView.getContext()).load(card_item.getOnePic()).into(imageOne);
                break;
        }

        imageSix.bringToFront();
        imageFive.bringToFront();
        imageFour.bringToFront();
        imageThree.bringToFront();
        imageTwo.bringToFront();
        imageOne.bringToFront();

        imageSix.setVisibility(View.VISIBLE);
        imageFive.setVisibility(View.VISIBLE);
        imageFour.setVisibility(View.VISIBLE);
        imageThree.setVisibility(View.VISIBLE);
        imageTwo.setVisibility(View.VISIBLE);
        imageOne.setVisibility(View.VISIBLE);

        // on click of next image, change the current image to gone and the next image to visible
        // if and only if card XYZ is visible and card XYZ+1 is not default
        nextImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (vFirst && !card_item.getTwoPic().equals("default")) {

                    imageTwo.setVisibility(View.VISIBLE);
                    imageOne.setVisibility(View.GONE);
                    //Toast.makeText(getContext(), card_item.getTwoPic().toString(), Toast.LENGTH_LONG).show();
                    vFirst = false;
                    vSecond = true;

                } else if (vSecond && !card_item.getThreePic().equals("default")) {

                    imageThree.setVisibility(View.VISIBLE);
                    imageTwo.setVisibility(View.GONE);
                    vSecond = false;
                    vThird = true;

                } else if (vThird && !card_item.getFourPic().equals("default")) {

                    imageFour.setVisibility(View.VISIBLE);
                    imageThree.setVisibility(View.GONE);
                    vThird = false;
                    vFourth = true;

                } else if (vFourth && !card_item.getFivePic().equals("default")) {

                    imageFive.setVisibility(View.VISIBLE);
                    imageFour.setVisibility(View.GONE);
                    vFourth = false;
                    vFifth = true;

                } else if (vFifth && !card_item.getSixPic().equals("default")) {

                    imageSix.setVisibility(View.VISIBLE);
                    imageFive.setVisibility(View.GONE);
                    vFifth = false;
                    vSixth = true;

                }

            }
        });

        // on click of prev image, change the current image to gone and the prev image to visible
        // if and only if card XYZ is visible and card XYZ-1 is not default
        prevImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (vSecond) {

                    imageOne.setVisibility(View.VISIBLE);
                    imageTwo.setVisibility(View.GONE);
                    vFirst = true;
                    vSecond = false;

                } else if (vThird && !card_item.getTwoPic().equals("default")) {

                    imageTwo.setVisibility(View.VISIBLE);
                    imageThree.setVisibility(View.GONE);
                    vSecond = true;
                    vThird = false;

                } else if (vFourth && !card_item.getThreePic().equals("default")) {

                    imageThree.setVisibility(View.VISIBLE);
                    imageFour.setVisibility(View.GONE);
                    vThird = true;
                    vFourth = false;

                } else if (vFifth && !card_item.getFourPic().equals("default")) {

                    imageFour.setVisibility(View.VISIBLE);
                    imageFive.setVisibility(View.GONE);
                    vFourth = true;
                    vFifth = false;

                } else if (vSixth && !card_item.getFivePic().equals("default")) {

                    imageFive.setVisibility(View.VISIBLE);
                    imageFour.setVisibility(View.GONE);
                    vFifth = true;
                    vSixth = false;

                }

            }
        });

        // TODO: show profile information page on click of more info section
        cardMoreInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        cardView.setVisibility(View.VISIBLE);

        return convertView;

    }

}