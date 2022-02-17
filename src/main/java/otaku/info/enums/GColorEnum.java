package otaku.info.enums;

import lombok.Getter;

@Getter
public enum GColorEnum {

    PALE_BLUE("1"),
    PALE_GREEN("2"),
    MAUVE("3"),
    PALE_RED("4"),
    YELLOW("5"),
    ORANGE("6"),
    CYAN("7"),
    GRAY("8"),
    BLUE("9"),
    GREEN("10"),
    RED("11");
        
    private final String id;

    GColorEnum(String id) {
        this.id = id;
    }
}
