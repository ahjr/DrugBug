package net.innit.drugbug.model;

public class FrequencyItem {
    private long id;
    private String label;
    private String timesOfDay;
    private Long interval;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getTimesOfDay() {
        return timesOfDay;
    }

    public void setTimesOfDay(String timesOfDay) {
        // Enforce either interval or times of day
        if (interval > 0) {
            interval = null;
        }
        this.timesOfDay = timesOfDay;
    }

    public Long getInterval() {
        return interval;
    }

    public void setInterval(long interval) {
        // Enforce either interval or times of day
        if (timesOfDay != null) {
            timesOfDay = null;
        }
        this.interval = interval;
    }

    public boolean usesInterval() {
        return interval != null;
    }

    public boolean usesTimesOfDay() {
        return timesOfDay != null;
    }

}
