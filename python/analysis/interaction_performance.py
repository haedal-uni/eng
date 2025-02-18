import pandas as pd
import seaborn as sns
import matplotlib
import matplotlib.pyplot as plt
import plotly.express as px
from scipy.stats import chi2_contingency
import scipy.stats as stats
import numpy as np
import scikit_posthocs as sp

data = pd.read_csv('../../kaggle/ONLINE EDUCATION SYSTEM REVIEW.csv')

print(data['Performance in online'].describe())
print(data['Your interaction in online mode'].describe())

# 1. 상호작용이 높을수록 학습성과가 높을 것이다.
fig = px.scatter(x='Your interaction in online mode', y='Performance in online', data_frame=data, trendline="ols") # 추세선 추가
fig.show()

sns.boxplot(x=data['Your interaction in online mode'], y=data['Performance in online'])
plt.show()


# 2. hist
sns.histplot(data["Your interaction in online mode"], bins=10, kde=True)
plt.show()

sns.histplot(data["Performance in online"], bins=10, kde=True)
plt.show()


# 3. 카이제곱
x_cut = pd.qcut(data["Your interaction in online mode"], q=3, duplicates='drop')
y_cut = pd.qcut(data["Performance in online"], q=3, duplicates='drop')

data["X_cut"] = pd.qcut(data["Your interaction in online mode"], q=len(x_cut.cat.categories), labels=['낮음', '높음'], duplicates='drop')
data["Y_cut"] = pd.qcut(data["Performance in online"], q=len(y_cut.cat.categories), labels=['낮음', '보통', '높음'], duplicates='drop')

observed = pd.crosstab(data['X_cut'], data['Y_cut'])
chi2, p, dof, expected = chi2_contingency(observed)
print(f"카이제곱 통계량: {chi2}, p-value: {p}")
print(f"자유도: {dof}")
print(f"기대 빈도표: \n{expected}")

# 기대 빈도 중 5 미만 셀 개수 확인
cell = (expected < 5).sum()
percentage = (cell / expected.size) * 100
if percentage >= 20:
    print("카이제곱 검정 사용 불가능")
else :
    print("카이제곱 검정 사용 가능")

if stats.chi2.ppf(0.95,dof).round(2) < chi2 and p <0.05 :
    print("대립가설 채택 (유의한 관계 있음)")
else :
    print("귀무가설 채택 (유의한 관계 없음)")

# 표준화 잔차
residuals = (observed - expected) / np.sqrt(expected)
print(residuals)


# 4. ANOVA
stat, p = stats.kstest(data['Your interaction in online mode'], 'norm')
if p > 0.05 :
    print("정규성 만족")
else :
    print("정규성 만족 X")


# 5. Kruskal-Wallis 검정
data["Y_Group"] = pd.qcut(data["Performance in online"], q=3, labels=["Low", "Medium", "High"])
low = data[data["Y_Group"] == "Low"]["Your interaction in online mode"]
medium = data[data["Y_Group"] == "Medium"]["Your interaction in online mode"]
high = data[data["Y_Group"] == "High"]["Your interaction in online mode"]

H, p = stats.kruskal(low, medium, high)
print(f"Kruskal-Wallis 결과: H-statistic={H}, p-value={p}")
if p < 0.05 :
    print("중앙값 차이 존재")
    print(p.round(2))

# 6. Dunn's test (Bonferroni 보정 적용)
dunn_result = sp.posthoc_dunn([low, medium, high], p_adjust='bonferroni')
print(dunn_result)