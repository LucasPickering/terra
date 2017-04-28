package me.lucaspickering.terra.input;

import java.util.HashMap;
import java.util.Map;

/**
 * A mapping between GLFW's key codes and strings. Each key code has a primary string mapping and
 * zero or more secondary string mappings.
 */
public enum KeyMapping {

    // Printable keys
    KEY_SPACE(32, "space"),
    KEY_APOSTROPHE(39, "'", "apostrophe"),
    KEY_COMMA(44, ",", "comma"),
    KEY_MINUS(45, "-", "comma"),
    KEY_PERIOD(46, ".", "period"),
    KEY_SLASH(47, "/", "slash", "forwardslash"),
    KEY_0(48, "0", "zero"),
    KEY_1(49, "1", "one"),
    KEY_2(50, "2", "two"),
    KEY_3(51, "3", "three"),
    KEY_4(52, "4", "four"),
    KEY_5(53, "5", "five"),
    KEY_6(54, "6", "six"),
    KEY_7(55, "7", "seven"),
    KEY_8(56, "8", "eight"),
    KEY_9(57, "9", "nine"),
    KEY_SEMICOLON(59, ";", "semicolon"),
    KEY_EQUAL(61, "=", "equal"),
    KEY_A(65, "a"),
    KEY_B(66, "b"),
    KEY_C(67, "c"),
    KEY_D(68, "d"),
    KEY_E(69, "e"),
    KEY_F(70, "f"),
    KEY_G(71, "g"),
    KEY_H(72, "h"),
    KEY_I(73, "i"),
    KEY_J(74, "j"),
    KEY_K(75, "k"),
    KEY_L(76, "l"),
    KEY_M(77, "m"),
    KEY_N(78, "n"),
    KEY_O(79, "o"),
    KEY_P(80, "p"),
    KEY_Q(81, "q"),
    KEY_R(82, "r"),
    KEY_S(83, "s"),
    KEY_T(84, "t"),
    KEY_U(85, "u"),
    KEY_V(86, "v"),
    KEY_W(87, "w"),
    KEY_X(88, "x"),
    KEY_Y(89, "y"),
    KEY_Z(90, "z"),
    KEY_LEFT_BRACKET(91, "[", "left_bracket"),
    KEY_BACKSLASH(92, "\\", "backslash"),
    KEY_RIGHT_BRACKET(93, "]", "right_bracket"),
    KEY_GRAVE_ACCENT(96, "`", "grave", "backtick"),

    // Function keys
    KEY_ESCAPE(256, "escape", "esc"),
    KEY_ENTER(257, "enter"),
    KEY_TAB(258, "tab"),
    KEY_BACKSPACE(259, "backspace"),
    KEY_INSERT(260, "insert", "ins"),
    KEY_DELETE(261, "delete", "del"),
    KEY_RIGHT(262, "right"),
    KEY_LEFT(263, "let"),
    KEY_DOWN(264, "down"),
    KEY_UP(265, "up"),
    KEY_PAGE_UP(266, "page_up", "pgup"),
    KEY_PAGE_DOWN(267, "page_down", "pgdown"),
    KEY_HOME(268, "home"),
    KEY_END(269, "end"),
    KEY_CAPS_LOCK(280, "caps_lock"),
    KEY_SCROLL_LOCK(281, "scroll_lock"),
    KEY_NUM_LOCK(282, "num_lock"),
    KEY_PRINT_SCREEN(283, "print_screen"),
    KEY_PAUSE(284, "pause"),
    KEY_F1(290, "f1"),
    KEY_F2(291, "f2"),
    KEY_F3(292, "f3"),
    KEY_F4(293, "f4"),
    KEY_F5(294, "f5"),
    KEY_F6(295, "f6"),
    KEY_F7(296, "f7"),
    KEY_F8(297, "f8"),
    KEY_F9(298, "f9"),
    KEY_F10(299, "f10"),
    KEY_F11(300, "f11"),
    KEY_F12(301, "f12"),
    KEY_F13(302, "f13"),
    KEY_F14(303, "f14"),
    KEY_F15(304, "f15"),
    KEY_F16(305, "f16"),
    KEY_F17(306, "f17"),
    KEY_F18(307, "f18"),
    KEY_F19(308, "f19"),
    KEY_F20(309, "f20"),
    KEY_F21(310, "f21"),
    KEY_F22(311, "f22"),
    KEY_F23(312, "f23"),
    KEY_F24(313, "f24"),
    KEY_F25(314, "f25"),
    KEY_KP_0(320, "kp_0", "num_0"),
    KEY_KP_1(321, "kp_1", "num_1"),
    KEY_KP_2(322, "kp_2", "num_2"),
    KEY_KP_3(323, "kp_3", "num_3"),
    KEY_KP_4(324, "kp_4", "num_4"),
    KEY_KP_5(325, "kp_5", "num_5"),
    KEY_KP_6(326, "kp_6", "num_6"),
    KEY_KP_7(327, "kp_7", "num_7"),
    KEY_KP_8(328, "kp_8", "num_8"),
    KEY_KP_9(329, "kp_9", "num_8"),
    KEY_KP_DECIMAL(330, "kp_decimal", "num_period"),
    KEY_KP_DIVIDE(331, "kp_divide", "num_div"),
    KEY_KP_MULTIPLY(332, "kp_multiply", "num_mult"),
    KEY_KP_SUBTRACT(333, "kp_subtract", "num_sub"),
    KEY_KP_ADD(334, "kp_add", "num_add"),
    KEY_KP_ENTER(335, "kp_enter", "num_enter"),
    KEY_KP_EQUAL(336, "kp_equal", "num_equal"),
    KEY_LEFT_SHIFT(340, "left_shift", "lshift"),
    KEY_LEFT_CONTROL(341, "left_control", "lctrl"),
    KEY_LEFT_ALT(342, "left_alt", "lalt"),
    KEY_LEFT_SUPER(343, "left_super", "lsuper"),
    KEY_RIGHT_SHIFT(344, "right_shift", "rshift"),
    KEY_RIGHT_CONTROL(345, "right_control", "rctrl"),
    KEY_RIGHT_ALT(346, "right_alt", "ralt"),
    KEY_RIGHT_SUPER(347, "right_super", "rsuper"),
    KEY_MENU(348, "menu");

    private static final Map<Integer, KeyMapping> intMappings = new HashMap<>();
    private static final Map<String, KeyMapping> stringMappings = new HashMap<>();

    static {
        // Initialize the int:KeyMapping and string:KeyMapping maps
        for (KeyMapping keyMapping : values()) {
            intMappings.put(keyMapping.code, keyMapping);
            stringMappings.put(keyMapping.primaryName, keyMapping);
            for (String secondaryName : keyMapping.secondaryNames) {
                stringMappings.put(secondaryName, keyMapping);
            }
        }
    }

    private final int code;
    private final String primaryName;
    private final String[] secondaryNames;

    KeyMapping(int code, String primaryName, String... secondaryNames) {
        this.code = code;
        this.primaryName = primaryName;
        this.secondaryNames = secondaryNames;
    }

    public static KeyMapping getByCode(int code) {
        return intMappings.get(code);
    }

    public static KeyMapping getByName(String name) {
        return stringMappings.get(name);
    }

    public int getCode() {
        return code;
    }

    public String getPrimaryName() {
        return primaryName;
    }

    public String[] getSecondaryNames() {
        return secondaryNames;
    }

    @Override
    public String toString() {
        return primaryName;
    }
}
