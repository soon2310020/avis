package saleson.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import saleson.common.enumeration.PageType;
import saleson.common.hibernate.converter.BooleanYnConverter;
import saleson.model.support.DateAudit;

import javax.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class BroadcastNotification extends DateAudit {
    @Id
    @GeneratedValue
    private Long id;

    private String message;
    private String valueListStr;
//    private String valueListZh;
    private String infoList;

//    @Transient
//    List<String> infors = new ArrayList<>();
    private String notificationType;//alert/system note

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User userTarget;
    @Enumerated(EnumType.STRING)
    private PageType systemFunction;

    @Column(name = "USER_ID", insertable = false, updatable = false)
    private Long userId;

    @Convert(converter = BooleanYnConverter.class)
    @Column(name = "IS_READ")
    private boolean isRead = false;
    private Instant seenTime;


    public void setInfors(List<String> list) {
        if (list != null) infoList = String.join(",", list);
        else infoList = null;
    }
    public List<String> getInfors(){
        List<String> list= new ArrayList<>();
        if(infoList!=null)
            list= Arrays.asList(infoList.split(","));
        return list;
    }
    public void setValueList(List<String> list) {
        if (list != null) valueListStr = String.join(",", list);
        else valueListStr = null;
    }
    public List<String> getValueList(){
        List<String> list= new ArrayList<>();
        if(valueListStr !=null)
            list= Arrays.asList(valueListStr.split(","));
        return list;
    }

}
