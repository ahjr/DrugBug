package net.innit.drugbug.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * ** NOT IN USE **
 * Implemented as a static list in DatabaseDAO object for now
 */
public class FrequencyItem {
    private String label;
    private List<Date> dates = new ArrayList<>();

    public String getLabel() {

        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

//    public List<Long> getOffsets() {
//        return offsets;
//    }
//
//    public void setOffsets(List<Long> offsets) {
//        this.offsets = offsets;
//    }
}
