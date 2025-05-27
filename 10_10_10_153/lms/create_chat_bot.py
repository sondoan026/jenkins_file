import shlex

from telegram import Update
from telegram.ext import Updater, CommandHandler, ContextTypes, ApplicationBuilder
import requests

AUTHORIZED_USER_ID = -4064083384  # Telegram user ID của bạn (bắt buộc để bảo mật)
TELEGRAM_TOKEN = '6102275063:AAHzH1m5fC_e4x1MdVeJl8aF-llVNtbjNpw'
JENKINS_URL = 'https://jenkins.scigroup.com.vn'
JENKINS_USER = 'root'
JENKINS_API_TOKEN = '11fc3e82557fe9d5f8c43e72230754a1b2'
JENKINS_JOB_NAME = 'tai_lieu_y_khoa'
JENKINS_JOB_TOKEN = 'pulltailieu'  # Token dùng để trigger job


async def pullcode(update: Update, context: ContextTypes.DEFAULT_TYPE):
    # Kiểm tra quyền
    # if update.effective_user.id != AUTHORIZED_USER_ID:
    #     await update.message.reply_text("🚫 Bạn không có quyền sử dụng lệnh này.")
    #     return

    try:
        session = requests.Session()

        # Lấy crumb
        crumb_url = f'{JENKINS_URL}/crumbIssuer/api/json'
        crumb_response = session.get(crumb_url, auth=(JENKINS_USER, JENKINS_API_TOKEN))
        if crumb_response.status_code != 200:
            await update.message.reply_text(f"❌ Không lấy được crumb: {crumb_response.status_code}")
            return
        crumb_data = crumb_response.json()
        headers = {
            crumb_data['crumbRequestField']: crumb_data['crumb']
        }

        # Parse tham số từ message
        message_text = update.message.text
        args =    shlex.split(message_text.replace("/pulltailieu", "", 1))
        param_dict = dict(arg.split("=", 1) for arg in args if "=" in arg)

        # Gửi yêu cầu tới Jenkins
        trigger_url = f"{JENKINS_URL}/job/{JENKINS_JOB_NAME}/buildWithParameters?token={JENKINS_JOB_TOKEN}"
        response = session.post(
            trigger_url,
            headers=headers,
            auth=(JENKINS_USER, JENKINS_API_TOKEN),
            data=param_dict
        )

        if response.status_code == 201:
            await update.message.reply_text(f"✅ Đã trigger Jenkins với tham số: {param_dict}")
        else:
            await update.message.reply_text(f"❌ Jenkins trả về lỗi: {response.status_code}\n{response.text}")

    except Exception as e:
        await update.message.reply_text(f"⚠️ Lỗi khi gọi Jenkins: {e}")

if __name__ == '__main__':
    app = ApplicationBuilder().token(TELEGRAM_TOKEN).build()
    app.add_handler(CommandHandler("pulltailieu", pullcode))
    print("🤖 Bot đang chạy...")
    app.run_polling()