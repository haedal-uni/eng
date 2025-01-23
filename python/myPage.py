from dotenv import load_dotenv
from urllib.parse import quote_plus
import os
from sqlalchemy import create_engine
import pandas as pd

import io
from flask import Flask, request, send_file
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt

load_dotenv()

# 환경 변수 가져오기
db_type = os.getenv("DB_TYPE")
db_driver = os.getenv("DB_DRIVER")
db_host = os.getenv("DB_HOST")
db_user = os.getenv("DB_USER")
db_password = os.getenv("DB_PASSWORD")
db_name = os.getenv("DB_NAME")
db_port = os.getenv("DB_PORT")

# 비밀번호 인코딩
encoded_password = quote_plus(db_password)

# SQLAlchemy 엔진 생성
engine = create_engine(
    f"{db_type}+{db_driver}://{db_user}:{encoded_password}@{db_host}:{db_port}/{db_name}"
)

def levelPie(username):
    query = "SELECT user.id FROM user WHERE username = %s"
    df = pd.read_sql(query, con=engine, params=(username,))
    user_id = df["id"][0]

    query = ("select s.level, count(s.level) cnt "
            "from sentence s "
            "join study st on s.id = st.sentence_id "
            "where st.user_id = %s "
            "group by s.level")
    df = pd.read_sql(query, con=engine, params=(user_id,))

    level_0 = df[df['level'] == 0]['cnt'].iloc[0]
    level_1 = df[df['level'] == 1]['cnt'].iloc[0]
    level_2 = df[df['level'] == 2]['cnt'].iloc[0]

    ratio = [level_0, level_1, level_2]
    labels = ['Easy', 'Medium', 'Hard']
    colors = ['#FF9999', '#FFCC99', '#99CCFF']
    explode = [0.03, 0.03, 0.03]

    plt.pie(ratio, labels=labels, autopct='%.1f%%',
            startangle=90, counterclock=False,
            colors=colors, explode=explode, shadow=True)

    img = io.BytesIO()
    plt.savefig(img, format='png')
    img.seek(0)
    plt.close()
    return send_file(img, mimetype='image/png')