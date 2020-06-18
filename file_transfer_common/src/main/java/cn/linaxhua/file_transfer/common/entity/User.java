package cn.linaxhua.file_transfer.common.entity;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class User {
    private Integer uid;
    private String name;
    private String password;
}
