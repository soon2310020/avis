package saleson.service.transfer;

import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import saleson.model.Transfer;

public class TransferRepositoryImpl extends QuerydslRepositorySupport implements TransferRepositoryCustom {
	public TransferRepositoryImpl() {
		super(Transfer.class);
	}

//    @Override
//    public List<TransactionDTO> getTransactionByDay(String day)
//    {
//        Instant dateTime = Instant.now();
//        List<TransactionDTO> transactionDTOList = new ArrayList<>();
////        QTransfer qTransfer;
//        QTransfer transfer = QTransfer.transfer;
//        QTransfer transferSub = QTransfer.transfer;
//        BooleanBuilder builder = new BooleanBuilder();
//        if(!StringUtils.isEmpty(day)){
//            Instant toDate= DateUtils.getInstant(day + "000000",DateUtils.DEFAULT_DATE_FORMAT).plus(1, ChronoUnit.DAYS);
//            builder.and(transfer.lst.between(day,DateUtils.getDay(toDate)));
//        }
//
//
//        JPQLQuery query = from(transfer)
//                .where(builder)
//                .groupBy(transfer.ti, transfer.ci)
//                .select(Projections.constructor(TransactionDTO.class, transfer.ti, transfer.ci, transfer.sc.sum(),transfer.sc.max())); // ct 100ms -> s
//        transactionDTOList = query.fetch();
//        for(TransactionDTO t: transactionDTOList ){
//            t.setDate(day);
//            t.setDatetime(dateTime.toEpochMilli());
//
//            BooleanBuilder builderSub = new BooleanBuilder();
//            if(!StringUtils.isEmpty(day)){
//                Instant toDate= DateUtils.getInstant(day + "000000",DateUtils.DEFAULT_DATE_FORMAT).plus(1, ChronoUnit.DAYS);
//                builderSub.and(transferSub.lst.between(day,DateUtils.getDay(toDate)));
//            }
//            Double cycleSub= JPAExpressions.select(transferSub.ct).from(transferSub).where(builderSub.and(transferSub.sc.eq(t.getScMax()))
//                    .and(transferSub.ci.eq(t.getCi()))
//                    .and(transferSub.ti.eq(t.getTi()))).fetchFirst();
//            t.setCt(cycleSub);
//        }
//
//        return transactionDTOList;
//    }
}
