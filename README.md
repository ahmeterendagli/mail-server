# mail-server

# send mail (post)example
# address
http://localhost:8080/api/mail/

# request body with mail adrress
[{
    "versionNumber": "1",
    "recipient": "aerendagli7@gmail.com",
    "subject": "Gmail SMTP Tes123",
    "body": "Hello, this is a test email sent via Gmail SMTP",
    "mailAddress": "karel@gmail.com",
    "mailPassword": "password",
    "smtpServerAddress": "smtp.gmail.com",
    "smtpServerPort": "587",
    "securityLayer": "TLS"
}]

# request body without mail address use active admin mail server
[{
"versionNumber": "1",
"recipient": "aerendagli7@gmail.com",
"subject": "Gmail SMTP Tes123",
"body": "Hello, this is a test email sent via Gmail SMTP"
}]
