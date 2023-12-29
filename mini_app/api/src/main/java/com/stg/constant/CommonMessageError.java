package com.stg.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommonMessageError {

    public static final String MSG02 = "Thông tin nghề nghiệp không hợp lệ. Chuyên viên tư vấn sẽ liên hệ bạn trong 24h hoặc vui lòng liên hệ Hotline MB Ageas 024 7309 9866 để được tư vấn và hỗ trợ";
    public static final String MSG12 = "Rất tiếc đã có lỗi xảy ra! Vui lòng thử lại sau";
    public static final String FL_MSG35 = "Chỉ được chọn 1 giá trị bổ sung cho từng lần thêm bảo hiểm bổ sung, không được mua trùng sản phẩm bổ sung";
    public static final String FL_MSG38 = "Độ tuổi tham gia bảo hiểm phải từ 30 ngày tuổi đến 65 tuổi. Quý khách vui lòng kiểm tra lại thông tin.";
    public static final String FL_MSG40 = "Độ tuổi tham gia bảo hiểm từ 15 ngày tuổi đến 65 tuổi. Quý khách vui lòng kiểm tra lại thông tin.";
    public static final String FL_MSG43 = "Tuổi của người được bảo hiểm bổ sung phải từ đủ 30 ngày tuổi đến 70 tuổi. Quý khách vui lòng kiểm tra lại thông tin.";
    public static final String FL_MSG46 = "Tuổi của người mua bảo hiểm bổ sung MIC phải từ đủ 18 tuổi trở lên.";

    public static final String MSG25 = "Độ tuổi tham gia bảo hiểm từ 18 - 60 tuổi. Quý khách vui lòng kiểm tra lại thông tin.";
    public static final String FL_MSG37 = "Gói sản phẩm bổ trợ chỉ dành cho Bên mua bảo hiểm và không dành cho người được bảo hiểm chính";

    // regex
    public static final String REGEX_DATE = "^\\d{4}\\-(0[1-9]|1[012])\\-(0[1-9]|[12][0-9]|3[01])$";

    public static final String MSG14 = "Tiến trình hết hạn. Vui lòng thực hiện lại.";

    public static final String MIC_MSG01 = "Số hợp đồng %s không phải hợp đồng MICCARE hoặc hết hiệu lực.";
    public static final String MIC_MSG02 = "Giấy chứng nhận bảo hiểm sức khỏe của Bố mẹ %s nhập đang không hợp lệ vì nhỏ hơn 18 tuổi, vui lòng chọn lại.";
    public static final String MIC_MSG03 = "Quyền lợi thai sản không được áp dụng cho độ tuổi ngoài 18-50";
    public static final String MSG13 = "Phiên làm việc đã hết hạn. Vui lòng quay lại trang chủ.";
    public static final String MIC_MSG04 = "Quyền lợi thai sản không được áp dụng cho giới tính nam";
    public static final String MIC_MSG05 = "Không tạo được giấy chứng nhận bảo hiểm MIC, vui lòng liên hệ nhân viên tư vấn để được hỗ trợ!";


    public static final String MSG_001 = "Số câu hỏi sức khỏe không hợp lệ";
    public static final String MSG_002 = "Thông tin sức khỏe của Quý khách chưa đủ để đáp ứng yêu cầu bảo hiểm. Vui lòng trả lời câu hỏi sức khỏe chi tiết hơn";
}

