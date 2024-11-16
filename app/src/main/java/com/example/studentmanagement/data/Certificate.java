package com.example.studentmanagement.data;

public class Certificate {
    private String id, name,  issueDate, issuer;

    public Certificate(){}
    public Certificate(String id, String name, String issueDate, String issuer){
        this.id = id;
        this.name = name;
        this.issueDate = issueDate;
        this.issuer = issuer;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(String issueDate) {
        this.issueDate = issueDate;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
}
