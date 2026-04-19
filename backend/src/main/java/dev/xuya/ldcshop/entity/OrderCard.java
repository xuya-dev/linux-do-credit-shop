package dev.xuya.ldcshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单卡密关联实体 / Order Card Relation Entity
 * 对应数据库 order_card 表，记录订单分配的卡密
 *
 * @author xuya
 */
@Data
@TableName("order_card")
public class OrderCard {

    /** ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单ID / Order ID */
    private Long orderId;

    /** 卡密ID / Card ID */
    private Long cardId;

    /** 创建时间 / Created Time */
    private LocalDateTime createdAt;
}
