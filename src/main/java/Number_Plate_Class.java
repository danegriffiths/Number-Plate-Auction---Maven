/**
 * Created by dane on 29/09/16.
 */
public class Number_Plate_Class {

    private String category;
    private String category_ID;
    private String numberPLate;
    private int guidePrice;
    private String dateTime;

    public Number_Plate_Class(String category, String category_ID, String plate, int price, String date) {

        this.category = category;
        this.category_ID = category_ID;
        this.numberPLate = plate;
        this.guidePrice = price;
        this.dateTime = date;
    }

    public String getNumberPLate() {
        return numberPLate;
    }

    public int getGuidePrice() {
        return guidePrice;
    }

    public String getDateTime() {
        return dateTime;
    }






}
