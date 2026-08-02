"""상호작용 수준과 학습 성과의 관계 분석.

데이터 : Kaggle - ONLINE EDUCATION SYSTEM REVIEW (1,033행)
가설   : 온라인 수업에서 상호작용 수준이 높을수록 학습 성과가 높을 것이다.

분석 순서
  1. 기술통계 / 분포 확인
  2. 상관분석  (Pearson으로 먼저 보고, 서열 척도라 Spearman을 채택)
  3. 정규성 확인 (Shapiro-Wilk)  -> 만족하지 않아 비모수 검정으로 진행
  4. 교차분석 (카이제곱 + 표준화 잔차)
  5. Kruskal-Wallis 검정
  6. Dunn 사후검정 (Bonferroni 보정)

그룹은 독립변수인 상호작용을 기준으로 나눈다.
  낮음 = 1~2 / 보통 = 3 / 높음 = 4~5
가설이 "상호작용에 따라 성과가 다른가"이므로, 상호작용으로 그룹을 나누고
성과를 비교해야 한다. (반대로 성과로 그룹을 나누면 같은 가설을 검증한 것이
아니게 되고 H값도 달라진다)

실행하면 나와야 하는 값
  Pearson  r = 0.558 / Spearman rho = 0.522 (p < 0.001)
  Shapiro-Wilk : 두 변수 모두 p < 0.001 (정규성 불만족)
  카이제곱 = 249.72 (dof=4, p < 0.001)
  Kruskal-Wallis H = 263.94 (p < 0.001)
  Dunn : 세 쌍 모두 p < 0.001
"""

import pandas as pd
import seaborn as sns
import matplotlib.pyplot as plt
import plotly.express as px
import scipy.stats as stats
import numpy as np
import scikit_posthocs as sp

X_COL = 'Your interaction in online mode'   # 상호작용 (1~5)
Y_COL = 'Performance in online'             # 학습 성과 (1~10)

data = pd.read_csv('../../kaggle/ONLINE EDUCATION SYSTEM REVIEW.csv')


#  1. 기술통계
print(data[Y_COL].describe())
print(data[X_COL].describe())

# 산점도 + 추세선
fig = px.scatter(x=X_COL, y=Y_COL, data_frame=data, trendline="ols")
fig.show()

sns.boxplot(x=data[X_COL], y=data[Y_COL])
plt.show()

sns.histplot(data[X_COL], bins=10, kde=True)
plt.show()

sns.histplot(data[Y_COL], bins=10, kde=True)
plt.show()


#  2. 상관분석
# Pearson은 두 변수가 연속형이고 선형 관계일 때를 전제한다.
# 여기서는 1~5, 1~10 짜리 서열(순위) 척도라 그 전제가 맞지 않는다.
# 그래서 값 대신 순위를 쓰는 Spearman을 최종 근거로 삼는다.
pearson_r, pearson_p = stats.pearsonr(data[X_COL], data[Y_COL])
spearman_r, spearman_p = stats.spearmanr(data[X_COL], data[Y_COL])
print(f"Pearson  r   = {pearson_r:.3f} (p={pearson_p:.3g})  <- 참고용")
print(f"Spearman rho = {spearman_r:.3f} (p={spearman_p:.3g})  <- 채택")

if spearman_p < 0.05:
    print("상호작용과 학습 성과 사이에 유의한 양의 상관관계가 있다")


#  3. 정규성 확인
# 주의: stats.kstest(v, 'norm')은 '평균 0, 표준편차 1'인 표준정규분포와 비교한다.
# 1~5 척도 데이터는 어떤 경우에도 기각되므로 정규성 판단 근거가 되지 못한다.
# 표본이 5,000개 미만이므로 Shapiro-Wilk를 쓴다.
for col in (X_COL, Y_COL):
    W, p = stats.shapiro(data[col])
    print(f"[정규성] {col}: W={W:.4f}, p={p:.3g} -> "
          f"{'만족' if p > 0.05 else '만족하지 않음'}")

print("두 변수 모두 정규성을 만족하지 않고 값의 종류도 5개/9개뿐인 이산 서열 척도라, "
      "평균 비교(ANOVA)가 아니라 순위 기반 비모수 검정으로 진행한다")


#  4. 그룹 나누기
# 독립변수(상호작용)를 기준으로 세 그룹으로 나눈다.
# pd.cut은 오른쪽을 포함하므로 (0,2] = 1~2, (2,3] = 3, (3,5] = 4~5 가 된다.
data["X_group"] = pd.cut(data[X_COL], bins=[0, 2, 3, 5],
                         labels=['낮음', '보통', '높음'])
print(data.groupby("X_group", observed=True)[Y_COL].agg(['count', 'median', 'mean']))


#  5. 교차분석
# 카이제곱은 "두 범주형 변수가 서로 관련이 있는가"를 본다.
# 성과도 범주로 만들어야 해서 3분위(하/중/상)로 나눈다.
data["Y_group"] = pd.qcut(data[Y_COL], q=3, labels=['하', '중', '상'])

observed = pd.crosstab(data["X_group"], data["Y_group"])
chi2, p, dof, expected = stats.chi2_contingency(observed)
print(observed)
print(f"카이제곱 통계량: {chi2:.2f}, p-value: {p:.3g}, 자유도: {dof}")

# 기대빈도가 5 미만인 칸이 전체의 20%를 넘으면 카이제곱을 쓸 수 없다.
low_cells = (expected < 5).sum()
if (low_cells / expected.size) * 100 >= 20:
    print("기대빈도가 작은 칸이 많아 카이제곱 검정 사용 불가")
else:
    print(f"기대빈도 5 미만 칸 {low_cells}개 -> 카이제곱 검정 사용 가능")

if p < 0.05:
    print("대립가설 채택 (유의한 관계 있음)")
else:
    print("귀무가설 채택 (유의한 관계 없음)")

# 표준화 잔차: 어느 칸이 기대보다 많고 적은지 본다. 절대값 2를 넘으면 눈에 띄는 칸.
residuals = (observed - expected) / np.sqrt(expected)
print("표준화 잔차:")
print(residuals.round(2))


#  6. Kruskal-Wallis
# 상호작용 그룹별로 '성과'를 비교한다.
low = data[data["X_group"] == '낮음'][Y_COL]
medium = data[data["X_group"] == '보통'][Y_COL]
high = data[data["X_group"] == '높음'][Y_COL]

H, p = stats.kruskal(low, medium, high)
print(f"Kruskal-Wallis 결과: H-statistic={H:.2f}, p-value={p:.3g}")
if p < 0.05:
    print("세 그룹의 학습 성과 중앙값에 차이가 있다")


#  7. Dunn 사후검정
# Kruskal-Wallis는 "어딘가 다르다"까지만 알려주므로, 어느 쌍이 다른지 확인한다.
# 세 번 비교하면 우연히 유의하게 나올 확률이 커져서 Bonferroni로 보정한다.
dunn_result = sp.posthoc_dunn([low, medium, high], p_adjust='bonferroni')
dunn_result.index = dunn_result.columns = ['낮음', '보통', '높음']
print("Dunn 사후검정 (Bonferroni 보정):")
print(dunn_result)