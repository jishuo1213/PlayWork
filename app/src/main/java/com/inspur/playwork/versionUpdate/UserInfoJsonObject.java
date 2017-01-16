package com.inspur.playwork.versionUpdate;

public class UserInfoJsonObject {

	private int Avatar ;
	private String ClientKEY;
	private String Company;
	private String CompanyId;
	private String Department;
	private String DepartmentId;
	private String EId;
	private String PersonType;
	private String SubDeptType;

	private String SubdepartmentId ;
	private String Subdepartment;
	private String UserName ;
	private String UserId ;
	private String Password ;
	private String VURL;


	// 后来加的版本信息
	private String Version ;
	private String VersionName ;
	private String Updatecontent ;

	public String getVersion() {
		return Version;
	}

	public void setVersion(String version) {
		Version = version;
	}

	public String getVersionName() {
		return VersionName;
	}

	public void setVersionName(String versionName) {
		VersionName = versionName;
	}

	public String getUpdatecontent() {
		return Updatecontent;
	}

	public void setUpdatecontent(String updatecontent) {
		Updatecontent = updatecontent;
	}

	public String getPassword() {
		return Password;
	}

	public void setPassword(String password) {
		Password = password;
	}

	public String getUserId() {
		return UserId;
	}

	public void setUserId(String userId) {
		UserId = userId;
	}

	public String getUserName() {
		return UserName;
	}

	public void setUserName(String userName) {
		UserName = userName;
	}

	public int getAvatar() {
		return Avatar;
	}

	public void setAvatar(int avatar) {
		Avatar = avatar;
	}

	public String getClientKEY() {
		return ClientKEY;
	}

	public void setClientKEY(String clientKEY) {
		ClientKEY = clientKEY;
	}

	public String getCompany() {
		return Company;
	}

	public void setCompany(String company) {
		Company = company;
	}

	public String getCompanyId() {
		return CompanyId;
	}

	public void setCompanyId(String companyId) {
		CompanyId = companyId;
	}

	public String getDepartment() {
		return Department;
	}

	public void setDepartment(String department) {
		Department = department;
	}

	public String getDepartmentId() {
		return DepartmentId;
	}

	public void setDepartmentId(String departmentId) {
		DepartmentId = departmentId;
	}

	public String getEId() {
		return EId;
	}

	public void setEId(String EId) {
		this.EId = EId;
	}

	public String getPersonType() {
		return PersonType;
	}

	public void setPersonType(String personType) {
		PersonType = personType;
	}

	public String getSubDeptType() {
		return SubDeptType;
	}

	public void setSubDeptType(String subDeptType) {
		SubDeptType = subDeptType;
	}

	public String getSubdepartment() {
		return Subdepartment;
	}

	public void setSubdepartment(String subdepartment) {
		Subdepartment = subdepartment;
	}

	public String getSubdepartmentId() {
		return SubdepartmentId;
	}

	public void setSubdepartmentId(String subdepartmentId) {
		SubdepartmentId = subdepartmentId;
	}

	public String getVURL() {
		return VURL;
	}

	public void setVURL(String VURL) {
		this.VURL = VURL;
	}


	@Override
	public String toString() {
		return "UserInfoJsonObject{" +
				"Avatar=" + Avatar +
				", ClientKEY='" + ClientKEY + '\'' +
				", Company='" + Company + '\'' +
				", CompanyId='" + CompanyId + '\'' +
				", Department='" + Department + '\'' +
				", DepartmentId='" + DepartmentId + '\'' +
				", EId='" + EId + '\'' +
				", PersonType='" + PersonType + '\'' +
				", SubDeptType='" + SubDeptType + '\'' +
				", SubdepartmentId='" + SubdepartmentId + '\'' +
				", Subdepartment='" + Subdepartment + '\'' +
				", UserName='" + UserName + '\'' +
				", UserId='" + UserId + '\'' +
				", Password='" + Password + '\'' +
				", VURL='" + VURL + '\'' +
				", Version='" + Version + '\'' +
				", VersionName='" + VersionName + '\'' +
				", Updatecontent='" + Updatecontent + '\'' +
				'}';
	}
}














