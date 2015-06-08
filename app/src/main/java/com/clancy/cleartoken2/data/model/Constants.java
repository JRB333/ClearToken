package com.clancy.cleartoken2.data.model;

import android.graphics.drawable.Drawable;

import com.clancy.cleartoken2.R;

public class Constants {

    // Colors info
    public enum ColorInfo{
        CT_RED (R.color.red_light, R.color.red_dark, R.color.pink_500, R.color.card_white),
        CT_GREEN (R.color.green_light, R.color.green_dark, R.color.pink_500, R.color.card_white),
        CT_PURPLE (R.color.purple_light, R.color.purple_dark, R.color.pink_500, R.color.card_white),
        CT_YELLOW (R.color.yellow_light, R.color.yellow_dark, R.color.pink_500, R.color.card_white),
        CT_BLUE (R.color.blue_light, R.color.blue_dark, R.color.pink_500, R.color.card_white),
        DEEP_PURPLE (R.color.deep_purple_500, R.color.deep_purple_700, R.color.pink_500, R.color.card_white),
        PURPLE (R.color.purple_500, R.color.purple_700, R.color.pink_500, R.color.card_white),
        AMBER (R.color.amber_500, R.color.amber_700, R.color.pink_500, R.color.card_white),
        INDIGO (R.color.indigo_500, R.color.indigo_700, R.color.pink_500, R.color.card_white),
        BLUE (R.color.blue_500, R.color.blue_700, R.color.pink_500, R.color.card_white),
        LIGHT_BLUE (R.color.light_blue_500, R.color.light_blue_700, R.color.pink_500, R.color.card_white),
        TEAL (R.color.teal_500, R.color.teal_700, R.color.pink_500, R.color.card_white),
        GREEN (R.color.green_500, R.color.green_700, R.color.pink_500, R.color.card_white),
        YELLOW (R.color.yellow_500, R.color.yellow_700, R.color.pink_500, R.color.text_black),
        GREY (R.color.grey_500, R.color.grey_700, R.color.pink_500, R.color.text_black),
        RED (R.color.red_500, R.color.red_700, R.color.pink_500, R.color.text_black);


        private final int primary;
        private final int secondary;
        private final int highlight;
        private final int textColor;
        private final int paidShape = 0;

        ColorInfo(int primary, int secondary, int highlight, int textColor) {
            this.primary = primary;
            this.secondary = secondary;
            this.highlight = highlight;
            this.textColor = textColor;
        }

        public int getPrimary() {
            return primary;
        }

        public int getSecondary() {
            return secondary;
        }

        public int getHighlight() {
            return highlight;
        }

        public int getTextColor() {
            return textColor;
        }

        /*public int getPaidShape() {
            int prim = getPrimary();
            int shape = 0;

            switch (prim) {
                case R.color.green_500:
                    shape = R.drawable.ic_paidshape_green;
                    break;
                case R.color.red_500:
                    shape = R.drawable.ic_paidshape_red;
                    break;
                case R.color.blue_500:
                    shape = R.drawable.ic_paidshape_blue;
                    break;
                case R.color.amber_500:
                    shape = R.drawable.ic_paidshape_yellow;
                    break;
                case R.color.purple_500:
                    shape = R.drawable.ic_paidshape_purple;
                    break;
                default:
                    shape = R.drawable.ic_fab_paid;
                    break;
            }
            return shape;
        }*/
    }
}
