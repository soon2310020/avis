package saleson.api.systemNote;

import org.springframework.data.jpa.repository.JpaRepository;
import saleson.model.SystemNote;
import saleson.model.SystemNoteRead;

public interface SystemNoteReadRepository  extends JpaRepository<SystemNoteRead, Long> {
}
