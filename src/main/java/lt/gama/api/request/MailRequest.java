package lt.gama.api.request;


import java.util.List;

public class MailRequest {

	private List<MailRequestContact> recipients;

	private String subject;

	private String msgBody;

	private Long docId;

	private String language;

	private String country;

	// generated

	public List<MailRequestContact> getRecipients() {
		return recipients;
	}

	public void setRecipients(List<MailRequestContact> recipients) {
		this.recipients = recipients;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getMsgBody() {
		return msgBody;
	}

	public void setMsgBody(String msgBody) {
		this.msgBody = msgBody;
	}

	public Long getDocId() {
		return docId;
	}

	public void setDocId(Long docId) {
		this.docId = docId;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	@Override
	public String toString() {
		return "MailRequest{" +
				"recipients=" + recipients +
				", subject='" + subject + '\'' +
				", msgBody='" + msgBody + '\'' +
				", docId=" + docId +
				", language='" + language + '\'' +
				", country='" + country + '\'' +
				'}';
	}
}
