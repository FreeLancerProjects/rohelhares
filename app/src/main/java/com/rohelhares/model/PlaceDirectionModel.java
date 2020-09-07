package com.rohelhares.model;

import java.io.Serializable;
import java.util.List;

public class PlaceDirectionModel implements Serializable {

    private List<Route> routes;

    public List<Route> getRoutes() {
        return routes;
    }

    public class Route
    {
        private List<Legs> legs;
        private Overview_polyline overview_polyline;

        public List<Legs> getLegs() {
            return legs;
        }

        public Overview_polyline getOverview_polyline() {
            return overview_polyline;
        }
    }
    public class Legs
    {
        private Distance distance;
        private Duration duration;
        private Location start_location;
        private Location end_location;
        private List<Steps> steps;

        public Distance getDistance() {
            return distance;
        }

        public Duration getDuration() {
            return duration;
        }

        public Location getStart_location() {
            return start_location;
        }

        public Location getEnd_location() {
            return end_location;
        }

        public List<Steps> getSteps() {
            return steps;
        }
    }
    public class Distance
    {
        private String text;
        private long value;

        public String getText() {
            return text;
        }

        public long getValue() {
            return value;
        }
    }
    public class Duration
    {
        private String text;
        private long value;

        public String getText() {
            return text;
        }

        public long getValue() {
            return value;
        }
    }
    public class Overview_polyline
    {
        private String points;

        public String getPoints() {
            return points;
        }
    }
    public class Location
    {
        private double lat;
        private double lng;

        public double getLat() {
            return lat;
        }

        public double getLng() {
            return lng;
        }
    }
    public class Steps
    {
        private Polyline polyline;

        public Polyline getPolyline() {
            return polyline;
        }
    }
    public class Polyline
    {
        private String points;

        public String getPoints() {
            return points;
        }
    }


}
