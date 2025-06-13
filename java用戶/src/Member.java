
public class Member {
    private String account;
    private String password; // 實際應用中，密碼應該加密儲存，這裡僅為演示
    private String dateOfBirth; // 使用字串格式，例如 "yyyy-MM-dd"
    private String phoneNumber;

    // 無參數構造函數 (Jackson 需要)
    public Member() {
    }

    public Member(String account, String password, String dateOfBirth, String phoneNumber) {
        this.account = account;
        this.password = password;
        this.dateOfBirth = dateOfBirth;
        this.phoneNumber = phoneNumber;
    }

    // Getters 和 Setters
    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getPhoneNumber() { 
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}