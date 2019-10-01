package com.g10.chit_chat.chatapp.utils.image;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageCategories {
    public final static Map<String, List<String>> CATEGORIES;
    static {
        CATEGORIES = new HashMap<String, List<String>>();

        // list food
        List<String> foods = new ArrayList<>();
        foods.add("dish");
        foods.add("food");
        foods.add("spaghetti");
        foods.add("sandwich");
        foods.add("burger");
        foods.add("rice");
        foods.add("fruit");
        foods.add("vegetable");
        foods.add("noodle");
        foods.add("bread");

        // list technology
        List<String> technologies = new ArrayList<>();
        technologies.add("laptop");
        technologies.add("phone");
        technologies.add("computer");
        technologies.add("robot");
        technologies.add("tv");
        technologies.add("television");
        technologies.add("calculator");
        technologies.add("electronic");
        technologies.add("device");

        // list building
        List<String> buildings = new ArrayList<>();
        buildings.add("build");
        buildings.add("landmark");
        buildings.add("house");

        List<String> screenshots = new ArrayList<String>();
        screenshots.add("screenshot");

        CATEGORIES.put("Food", foods);
        CATEGORIES.put("Technology", technologies);
        CATEGORIES.put("Buildings", buildings);
        CATEGORIES.put("Screenshots", screenshots);
    }
}
