package com.stg.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "mic_package")
public class MicPackage implements Serializable {

    private static final long serialVersionUID = 5240145658170903349L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", unique = true, nullable = false)
    private String name;

    //Tử vong/thương tật vĩnh viễn do tai nạn
    @Column(name = "main_one")
    private  String mainOne;

    //Chi phí y tế do tai nạn
    @Column(name = "main_two")
    private  String mainTwo;

    //Điều trị nội trú do ốm đau, bệnh tật
    @Column(name = "main_three")
    private  String mainThree;

    //Nằm viện do ốm đau, bệnh tật (tối đa 60 ngày/năm)
    @Column(name = "main_three_one")
    private String mainThreeOne;

    //Phẫu thuật do ốm đau, bệnh tật hoặc liên quan đến cấy ghép nội tạng
    @Column(name = "main_three_two")
    private String mainThreeTwo;

    //Điều trị trước/ sau khi nhập viện hoặc y tế chăm sóc tại nhà sau xuất viện
    @Column(name = "main_three_three")
    private String mainThreeThree;

    //Trợ cấp nằm viện/ngày (tối đa 60 ngày)
    @Column(name = "main_three_four")
    private String mainThreeFour;

    //Dịch vụ xe cứu thương
    @Column(name = "main_three_five")
    private String mainThreeFive;

    //Phục hồi chức năng
    @Column(name = "main_three_six")
    private String mainThreeSix;

    //Trợ cấp mai táng phí
    @Column(name = "main_three_seven")
    private String mainThreeSeven;

    //ĐKBS 01 - Ngoại trú do ốm đau, bệnh tật
    @Column(name = "sub_one")
    private String subOne;

    //Chi phí khám ngoại trú
    @Column(name = "sub_one_one")
    private String subOneOne;

    //Vật lý trị liệu
    @Column(name = "sub_one_two")
    private String subOneTwo;

    //ĐKBS 02 - Quyền lợi nha khoa
    @Column(name = "sub_two")
    private String subTwo;

    //ĐKBS 03 - Quyền lợi thai sản
    @Column(name = "sub_three")
    private String subThree;

    //ĐKBS 04 - Tử vong, thương tật toàn bộ vĩnh viễn do ốm đau, bệnh tật
    @Column(name = "sub_four")
    private String subFour;

}
