package saleson.dto;

import lombok.Data;

import java.util.List;

@Data
public class RestDataList<T>
{
    private long total;
    private List<T> dataList;

    public RestDataList(long total, List dataList)
    {
        this.total = total;
        this.dataList = dataList;
    }
}
