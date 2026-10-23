package com.allst.es.scenario.geo;

import lombok.Data;

@Data
public class PlaceCommand {
    private String id;
    private String name;
    private String category;
    private String city;
    private String address;
    private Double lat;
    private Double lon;
    private Double rating;
}
