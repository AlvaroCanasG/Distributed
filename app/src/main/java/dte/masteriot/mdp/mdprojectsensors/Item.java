package dte.masteriot.mdp.mdprojectsensors;

public class Item {
    // This class contains the actual data of each item of the dataset

    private String title;

    private long key;
    private boolean status;

    private int image;
    private Integer Inside;
    private Integer Arriving;

    private String Error;

    //Item(String title, String stringURI, String subtitle, Long key, int image, boolean status) {
        Item(String title, Integer Arriving,Integer Inside, String Error, long key, int image , boolean status) {
        this.title = title;
        this.key = key;
        this.image = image;
        this.Inside = Inside;
        this.Arriving = Arriving;
        this.Error = Error;
        this.status = status;
    }

    public String getTitle() {
        return title;
    }


    public long getKey() {
        return key;
    }

    public int getImage(){ return image;}

    public Integer getInside(){return Inside; }
    public Integer getArriving(){ return Arriving;}
    public String getError(){ return Error;}
    public void setArriving (String arriving){
            Arriving = Integer.parseInt(arriving);
    }


    public void setParameters(Integer arriving, Integer inside, String error){
            Inside = inside;
            Arriving = arriving;
            Error = error;
    }

    public boolean getStatus(){return status; }

    public void setStatus(boolean Status){
            status = Status;
    }

    // We override the "equals" operator to only compare keys
    // (useful when searching for the position of a specific key in a list of Items):
    public boolean equals(Object other) {
        return this.key == ((Item) other).getKey();
    }

}