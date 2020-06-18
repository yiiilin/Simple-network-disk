package cn.linaxhua.file_transfer.common.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
public class Structure {
    private Integer id;
    private Integer uid;
    private String path;
    private String uuid;
    private String name;
    private Long size;
    private Date update_time;
    private String type;
}
