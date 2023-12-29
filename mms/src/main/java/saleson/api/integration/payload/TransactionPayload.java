package saleson.api.integration.payload;


import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import saleson.model.QTransfer;

import java.time.Instant;

public class TransactionPayload {
    private Long fromTime;
    private Long toTime;
    public Predicate getPredicate() {
        QTransfer transfer=QTransfer.transfer;
        BooleanBuilder builder = new BooleanBuilder();

        if(fromTime!=null){
            builder.and(transfer.createdAt.after(Instant.ofEpochMilli(fromTime)));
        }
        if(toTime!=null){
            builder.and(transfer.createdAt.before(Instant.ofEpochMilli(toTime)));
        }

        return builder;
    }
}
