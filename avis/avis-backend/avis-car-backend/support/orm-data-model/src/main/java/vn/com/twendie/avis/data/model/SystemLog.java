package vn.com.twendie.avis.data.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "system_log")
@Getter
@Setter
@NoArgsConstructor
public class SystemLog extends BaseModel implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", insertable = false, nullable = false)
    private Long id;

    private String user;
    private Long userId;
    private String ip;
    private String device="MOBILE";//MOBILE, WEB
    @Column(name = "`level`")
    private String level;// ERROR, WARNING, INFO
    @Column(name = "`type`")
    private String type="CLIENT";// CLIENT, SERVER
    private Instant callTime;
    @Column(name = "`function`")
    private String function;
    @Lob
    private String logs;

}
