package saleson.dto.exports.dynamic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.CycleTimeStatus;
import saleson.common.enumeration.EfficiencyStatus;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoldItem implements Comparable<MoldItem> {
    private String moldCode;
    private String groupHeader;
    private String groupHeaderView;
    private String childHeader;
    private String title;//for mapping data
    private String shortCount;
    private String ct;
    private String uptimeHour;
    private String temperature;

    //column for cal
    private CycleTimeStatus cycleTimeStatus;
    private EfficiencyStatus uptimeStatus;
    private Instant timeSort;
    private int indexInHour=0;
    //col for build header child(header level 2)
    private int maxItemInHour=1;
    //for group hearder(level1)
    private int lastIndexCol;
    private int numColInGroup;
    public String getKeyHeaderChild(){
        String key=groupHeader+"_"+ childHeader;
        return key;
    }
    public String getKeyColumn(){
        String key=getKeyHeaderChild() +"_"+indexInHour;
        return key;
    }

    @Override
    public int compareTo(MoldItem o) {
        int val = Long.compare(timeSort!=null?timeSort.toEpochMilli():0, o.getTimeSort()!=null?o.getTimeSort().toEpochMilli():0);
        if(val == 0) Integer.compare(indexInHour,o.getIndexInHour());
        return val;
    }
}
