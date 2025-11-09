package app.service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    @Async
    public void sendNotificationEmails(String url, String to, String username) {

        try {
            MimeMessage emails = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(emails, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Notifikasi - Livechat");
            helper.setFrom("agmerramadhan@gmail.com");
            String html = buildLiveChatEmailHtml(username, url);

            helper.setText(buildLiveChatPlain(html), html);

            mailSender.send(emails);

        } catch (MessagingException e) {
            e.printStackTrace();
        }

    }

    @Async
    public void sendPasswordResetEmail(String to, String resetLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject("Reset Password — Tindakan Diperlukan");
            helper.setFrom("agmerramadhan@gmail.com");
            String html = buildResetEmailHtml(resetLink);
            String plain = buildResetEmailPlain(resetLink);
            helper.setText(plain, html);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Gagal mengirim email reset password", e);
        }
    }

    private String buildResetEmailHtml(String link) {
        return """
                <!doctype html>
                <html lang="en">
                <head>
                    <meta charset="utf-8">
                    <meta name="viewport" content="width=device-width,initial-scale=1">
                    <title>Reset Password</title>
                </head>
                <body style="margin:0;padding:0;background:#f4f6f8;font-family:Inter, 'Segoe UI', Roboto, Arial, sans-serif;">
                    <table role="presentation" style="width:100%%;border-collapse:collapse;background:#f4f6f8;padding:30px 0;">
                        <tr>
                            <td align="center">
                                <table role="presentation" style="width:600px;max-width:94%%;background:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 6px 18px rgba(0,0,0,0.06);">
                                    <tr>
                                        <td style="padding:24px 28px;border-bottom:1px solid #eef2f6;">
                                            <h1 style="margin:0;font-size:20px;color:#0f172a;">Reset password kamu</h1>
                                            <p style="margin:6px 0 0;color:#475569;font-size:14px;">Kami menerima permintaan untuk mereset password akunmu. Klik tombol di bawah untuk melanjutkan.</p>
                                        </td>
                                    </tr>

                                    <tr>
                                        <td style="padding:32px 28px;text-align:center;">
                                            <a href="%s" target="_blank" rel="noopener noreferrer"
                                               style="display:inline-block;padding:12px 22px;background:#2563eb;color:#ffffff;text-decoration:none;border-radius:8px;font-weight:600;">
                                               Reset Password
                                            </a>

                                            <p style="margin:18px 0 0;color:#64748b;font-size:13px;">
                                                Jika tombol tidak bekerja, salin dan tempel URL berikut di browser:
                                            </p>

                                            <p style="word-break:break-all;background:#f1f5f9;padding:10px;border-radius:6px;color:#0f172a;font-size:13px;">
                                                <a href="%s" style="color:#0f172a;text-decoration:underline;">%s</a>
                                            </p>

                                            <p style="margin-top:10px;color:#94a3b8;font-size:12px;">
                                                Link ini hanya berlaku sementara. Jika kamu tidak meminta reset password, abaikan email ini.
                                            </p>
                                        </td>
                                    </tr>

                                    <tr>
                                        <td style="padding:18px 28px;background:#f8fafc;border-top:1px solid #eef2f6;text-align:center;color:#94a3b8;font-size:12px;">
                                            © %d Boysan multimedia. Semua hak cipta dilindungi.
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """
                .formatted(link, link, link, java.time.Year.now().getValue());
    }

    private String buildResetEmailPlain(String link) {
        return """
                Reset password

                Kami menerima permintaan untuk mereset password akun Anda.
                Buka link berikut untuk mengganti password:
                %s

                Link ini hanya berlaku sementara. Jika Anda tidak meminta reset password, abaikan email ini.

                — BoysanMultimedia
                """.formatted(link);
    }

    private String buildLiveChatPlain(String html) {

        return """
                Sistem Notifikasi!

                Seseorang menghubungi kamu di livechat!

                %s
                """.formatted(html);

    }

    private String buildLiveChatEmailHtml(String userName, String liveChatUrl) {
        return """
                <!doctype html>
                <html lang="id">
                <head>
                    <meta charset="utf-8">
                    <meta name="viewport" content="width=device-width,initial-scale=1">
                    <title>Notifikasi Live Chat</title>
                </head>
                <body style="margin:0;padding:0;background:#f4f6f8;font-family:Inter, 'Segoe UI', Roboto, Arial, sans-serif;">
                    <table role="presentation" style="width:100%%;border-collapse:collapse;background:#f4f6f8;padding:30px 0;">
                        <tr>
                            <td align="center">
                                <table role="presentation" style="width:600px;max-width:94%%;background:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 6px 18px rgba(0,0,0,0.06);">
                                    <tr>
                                        <td style="padding:24px 28px;border-bottom:1px solid #eef2f6;">
                                            <h1 style="margin:0;font-size:20px;color:#0f172a;">💬 Pesan Baru di Live Chat</h1>
                                            <p style="margin:6px 0 0;color:#475569;font-size:14px;">
                                                Anda menerima pesan baru dari: <strong>%s</strong>.
                                            </p>
                                            <p style="margin:6px 0 0;color:#475569;font-size:14px;">
                                                Klik tombol di bawah untuk membalas pesan dan membuka Live Chat.
                                            </p>
                                        </td>
                                    </tr>

                                    <tr>
                                        <td style="padding:32px 28px;text-align:center;">
                                            <a href="%s" target="_blank" rel="noopener noreferrer"
                                               style="display:inline-block;padding:12px 22px;background:#2563eb;color:#ffffff;text-decoration:none;border-radius:8px;font-weight:600;">
                                               Buka Live Chat
                                            </a>

                                            <p style="margin:18px 0 0;color:#64748b;font-size:13px;">
                                                Jika tombol tidak bekerja, salin dan tempel URL berikut di browser:
                                            </p>

                                            <p style="word-break:break-all;background:#f1f5f9;padding:10px;border-radius:6px;color:#0f172a;font-size:13px;">
                                                <a href="%s" style="color:#0f172a;text-decoration:underline;">%s</a>
                                            </p>

                                            <p style="margin-top:10px;color:#94a3b8;font-size:12px;">
                                                Email ini hanya pemberitahuan. Abaikan jika Anda tidak sedang menggunakan Live Chat.
                                            </p>
                                        </td>
                                    </tr>

                                    <tr>
                                        <td style="padding:18px 28px;background:#f8fafc;border-top:1px solid #eef2f6;text-align:center;color:#94a3b8;font-size:12px;">
                                            © %d YourApp. Semua hak cipta dilindungi.
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """
                .formatted(userName, liveChatUrl, liveChatUrl, liveChatUrl, java.time.Year.now().getValue());
    }

}
