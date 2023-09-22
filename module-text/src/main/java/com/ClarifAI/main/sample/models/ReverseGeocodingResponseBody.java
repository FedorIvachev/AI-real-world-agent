package com.ClarifAI.main.sample.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ReverseGeocodingResponseBody {
@SerializedName("returnCode")
        public String returnCode;
        @SerializedName("sites")
        public List<Site> sites;
        @SerializedName("returnDesc")
        public String returnDesc;

    public class Site {
        @SerializedName("formatAddress")
        private String formatAddress;
        @SerializedName("address")
        private Address address;
        @SerializedName("aoiFlag")
        private boolean aoiFlag;
        @SerializedName("poi")
        private Poi poi;
        @SerializedName("viewport")
        private Viewport viewport;
        @SerializedName("name")
        public String name;
        @SerializedName("siteId")
        private String siteId;
        @SerializedName("location")
        private Location location;
        // getters and setters
    }

    public class Address {
        @SerializedName("country")
        private String country;
        @SerializedName("city")
        private String city;
        @SerializedName("countryCode")
        private String countryCode;
        @SerializedName("tertiaryAdminArea")
        private String tertiaryAdminArea;
        @SerializedName("postalCode")
        private String postalCode;
        @SerializedName("adminArea")
        private String adminArea;
        @SerializedName("subAdminArea")
        private String subAdminArea;
        // getters and setters
    }

    public class Poi {
        @SerializedName("rating")
        private double rating;
        @SerializedName("poiTypes")
        private List<String> poiTypes;
        @SerializedName("childrenNodes")
        private List<Object> childrenNodes; // Replace Object with the actual type if known
        @SerializedName("hwPoiTypes")
        private List<String> hwPoiTypes;
        @SerializedName("internationalPhone")
        private String internationalPhone;
        // getters and setters
    }

    public class Viewport {
        @SerializedName("southwest")
        private Location southwest;
        @SerializedName("northeast")
        private Location northeast;
        // getters and setters
    }

    public class Location {
        @SerializedName("lng")
        private double lng;
        @SerializedName("lat")
        private double lat;
        // getters and setters
    }

}
