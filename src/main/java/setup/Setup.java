package setup;

public class Setup {

	/** Does not compile under Java 1.8 */
//	boolean isTodayHoliday(String dayName) {
//		return switch (day) {
//            case "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY" -> false;
//            case "SATURDAY", "SUNDAY" -> true;
//            default -> throw new IllegalArgumentException("What's a " + dayName + "?");
//    };
    
    
    /** Java 8 version */
    boolean isTodayHoliday8(String dayName) {
        switch (dayName) {
            case "MONDAY":
            case "TUESDAY":
            case "WEDNESDAY":
            case "THURSDAY":
            case "FRIDAY":
                return false;
            case "SATURDAY":
            case "SUNDAY":
                return true;
            default:
                throw new IllegalArgumentException("What's a " + dayName + "?");
        }
    }
}
