package saleson.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import saleson.common.enumeration.BatchStatus;
import saleson.common.enumeration.MessageType;
import saleson.model.support.DateAudit;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder @EqualsAndHashCode(of = "id", callSuper=false)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "createdAt", "updatedAt"})
@Table(indexes = {@Index(name = "IDX_BATCH_STATUS_MESSAGE_TYPE", columnList = "batchStatus, messageType")})
public class BatchMessage extends DateAudit {

	@Id
	@GeneratedValue
	private Long id;

	@Enumerated(EnumType.STRING)
	private MessageType messageType;

	private String title;

	@Lob
	private String content;

	@Lob
	private String receivers;
	private String sender;

	@Enumerated(EnumType.STRING)
	private BatchStatus batchStatus;

	private LocalDateTime startTime;
	private LocalDateTime endTime;

	@Lob
	private String batchResult;

}
