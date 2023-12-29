package saleson.dto;

import lombok.Data;

import java.util.List;

@Data
public class RestData
{
    private long total;
    private List<Object> dataList;

    public RestData()
    {
    }

    public RestData(long total, List dataList)
    {
        this.total = total;
        this.dataList = dataList;
    }
}
