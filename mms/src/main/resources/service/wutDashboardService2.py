import pandas as pd
from pandas import json_normalize
import matplotlib.pyplot as plt
import os
from collections import Counter
import numpy as np
import shutil
from datetime import datetime
import time
import sys
import json
pd.options.mode.chained_assignment = None

# Set the file directory and read the .csv file as dataframe

# dir = r'C:/Users/USER_20210715/Documents/Data/Delonghi/json/'
# fl = 'NCM2042I01035_202120.json'
# dir_fl = dir + fl
#
# rd_json = open(dir_fl)
# json_data = json.load(rd_json)
def calculateWarmUpTime(json_data):
    #df_org = json_normalize(json_data['data'])
    df_org = json_normalize(json_data)

    # dir = r'C:/Users/USER_20210715/Documents/Data/Delonghi/'
    # # fl = 'NCM2025I01006.csv'
    # fl = 'NCM2025I01023.csv'
    # dir_fl = dir + fl
    # df_org = pd.read_csv(dir_fl)

    # Change the RT column into date format
    # Select necessary columns and sort by dates
    # Only select dates from 2021 and above
    df = df_org[['CI', 'CT', 'RT', 'TAV', 'CTT', 'LST', 'TEMP', 'TFF']]
    df['RT_time'] = df['RT'].apply(lambda x: pd.to_datetime(str(x)))
    df = df.sort_values(by=['RT_time'])
    mask = (df['RT_time'] > '2021-01-01 00:00:00')
    df = df.loc[mask]


    # Generate rt_ls, a list of unique month and day
    # Select the date of interest
    # Select the day before the day of interest
    df['Mon_day'] = df['RT_time'].dt.strftime('%m-%d')
    rt_ls = df['Mon_day'].unique()
    df_final = pd.DataFrame(columns= ['index_col', 'risk', 'shot_count', 'time', 'date'])




    def plot_fig(df_s1, det_sect, T_stab = -1):

        # plt.figure
        # ax = df_s1.plot('index_col', 'TAV')
        # plt.xlabel("index")

        if det_sect == 0:
            plt.figure
            ax = df_s1.plot('index_col', 'TAV')
            plt.title("Average Temperature vs time")
            plt.xlabel("Time Index")
            for i in range(len(df_s1)):
                plt.text(df_s1['index_col'].values[i], T_stab - 5, df_s1['risk'].values[i], fontsize=7)
            plt.text(2, df_s1['TAV'].mean(), "Normal Production Time", fontsize=10)


        if det_sect == 1:
            plt.figure
            ax = df_s1.plot('index_col', 'TAV')
            plt.title("Average Temperature vs time")
            plt.xlabel("Time Index")
            plt.text(2, df_s1['TAV'].mean(), "No Production Time", fontsize=10)

        if det_sect == 2 or det_sect == 3:
            plt.figure
            ax = df_s1.plot('index_col', 'TAV')
            plt.title("Average Temperature vs time and Discretization")
            plt.xlabel("Time Index")
            df_s1.plot('index_col', 'Discr', secondary_y=True, ax=ax)
            for i in df_s1['Point']:
                if i >= 0:
                    tmp = df_s1[df_s1['Point'] == i]
                    pt = tmp['Point'].values[0]
                    x = tmp['index_col'].values[0]
                    y = tmp['Discr'].values[0]
                    plt.text(x, y, pt, fontsize=10)

        # dir = r'C:/Users/USER_20210715/Documents/PatternAnalysis/figs/'
        # dir_fig = dir +str(fl[:-4])+'/'+str(sel_day)+'.png'
        # plt.savefig(dir_fig)
        # plt.show()



    def step_5(df_s1, TAV_avg):
        T_stab = df_s1['TAV'].mean()
        abnorm = []
        for i in df_s1['TAV']:
            if T_stab - 50 > i:
                abnorm.append("medium")
            if T_stab + 50 < i:
                abnorm.append("medium")
            if T_stab - 50 <= i <= T_stab + 50:
                abnorm.append("low")
        df_s1['risk'] = abnorm

        return df_s1, T_stab


    def step_4_2(df_s1, TAV_avg):
        # plt.figure
        # ax = df_s1.plot('index_col', 'TAV')
        # plt.title("TAV")
        # plt.xlabel("index")

        if TAV_avg > 350:
            # print("Normal Production Time")
            det_sec_ls = []
            for i in range(len(df_s1)):
                det_sec_ls.append(0)
            df_s1['det_sect'] = det_sec_ls

            # plt.text(2, df_s1['TAV'].mean(), "Normal Production Time", fontsize=10)

            df_s1, T_stab = step_5(df_s1, TAV_avg)
            for i in range(len(df_s1)):
                plt.text(df_s1['index_col'].values[i], T_stab - 5, df_s1['risk'].values[i], fontsize=7)

            plot_fig(df_s1, 0, T_stab)

        elif TAV_avg <= 350:
            # print("No Production Time")
            det_sec_ls = []
            ct_mal_cnt = []
            for i in range(len(df_s1)):
                det_sec_ls.append(1)
                ct_mal_cnt.append(None)
            plot_fig(df_s1, 1)

        df_s1['det_sect'] = det_sec_ls
        df_s1['risk'] = 'low'


        return df_s1


    def step_4_1(df_s1, ABS_avg):
        # Discretization condition
        disc = []
        ABS_avg_rnd = round(ABS_avg)
        diff_tmp_ls = df_s1['Diff_Temp'].to_list()

        for i in range(len(df_s1)):
            diff_tmp = diff_tmp_ls[i]
            if -ABS_avg_rnd <= diff_tmp < ABS_avg_rnd:
                tmp = 0
            if diff_tmp < -ABS_avg_rnd:
                tmp = -30
            if diff_tmp > ABS_avg_rnd:
                tmp = 30

            disc.append(tmp)

        df_s1['Discr'] = disc


        # Determine the warm-up and cool-down start points
        # -1 is a null point
        pnt_ls = []
        for i in range(len(df_s1)):
            pnt_ls.append(-1)

        for i in range(len(df_s1) - 1):
            i_val = i
            tmp1 = disc[i]
            tmp2 = disc[i + 1]

            if i != 0:
                tmp0 = disc[i - 1]
                if (tmp0 == 0 and tmp1 == 30):
                    pnt_ls[i] = 0
                    pnt_ls[i - 1] = 9

                if (tmp0 == -30 and tmp1 == 0 and tmp2 == 30):
                    pnt_ls[i] = 0

                if (tmp0 == 0 and tmp1 == -30):
                    pnt_ls[i] = 2
                    pnt_ls[i - 1] = 7

                if (tmp0 == 30 and tmp1 == 0 and tmp2 == -30):
                    pnt_ls[i] = 2

            if (tmp1 == 30 and tmp2 == 0):
                pnt_ls[i] = 1
                pnt_ls[i + 1] = 6

            if (tmp1 == -30 and tmp2 == 0):
                pnt_ls[i] = 3
                pnt_ls[i + 1] = 8


        for i in range(1, len(pnt_ls) - 1):

            tmp0 = pnt_ls[i - 1]
            tmp1 = pnt_ls[i]
            tmp2 = pnt_ls[i + 1]

            if tmp0 == 3 and tmp2 == 0:
                pnt_ls[i] = 4

            if tmp0 == 1 and tmp2 == 2:
                pnt_ls[i] = 5
            # print()

        df_s1['Point'] = pnt_ls


        det_sec_ls = []
        for i in range(len(pnt_ls) - 1):
            tmp1 = pnt_ls[i]
            tmp2 = pnt_ls[i + 1]

            if tmp1 != tmp2:

                if (tmp2 == 0) | (tmp2 == 1):
                    det_sec_ls.append([i + 1, 2])

                if (tmp2 == 2) | (tmp2 == 3):
                    det_sec_ls.append([i + 1, 3])

                if (tmp2 == 4):
                    det_sec_ls.append([i, 2])

                if (tmp2 == 5):
                    det_sec_ls.append([i + 1, 3])

                if (tmp2 == 6) | (tmp2 == 7):
                    det_sec_ls.append([i + 1, 0])

                if (tmp2 == 8) | (tmp2 == 9):
                    det_sec_ls.append([i + 1, 1])

        det_sec_ls_ln = len(det_sec_ls)
        cnt = 0
        det_sec_clmn = []
        rng = 0
        df_len = len(df_s1)
        while cnt <= det_sec_ls_ln - 1:
            tmp = det_sec_ls[cnt]
            lngth = int(tmp[0])
            det = tmp[1]
            if cnt == (det_sec_ls_ln - 1):
                det_sec_clmn_ln = len(det_sec_clmn)
                x = df_len - det_sec_clmn_ln
                for i in range(x):
                    det_sec_clmn.append(det)

            else:
                if det_sec_ls_ln == 2:
                    x = lngth - rng + 1
                if det_sec_ls_ln > 2:
                    if cnt == 0:
                        x = lngth - rng + 1
                    else:
                        x = lngth - rng
                for i in range(x):
                    det_sec_clmn.append(det)

            cnt += 1
            rng = lngth

        df_s1['det_sect'] = det_sec_clmn
        risk_ls = []
        norm_ls = []
        for i in range(len(df_s1)):
            tmp = df_s1.iloc[i]['det_sect']

            if i < len(df_s1) - 1:
                tmp_n = df_s1.iloc[i + 1]['det_sect']
                if tmp == 0 & tmp_n != 0:
                    grp += 1
            grp = 0
            if tmp == 0:
                norm_ls.append(grp)
            if tmp != 0:
                norm_ls.append(-1)
        for i in range(1, len(pnt_ls) - 1):

            tmp0 = pnt_ls[i - 1]
            tmp1 = pnt_ls[i]
            tmp2 = pnt_ls[i + 1]

            if tmp0 == 3 and tmp2 == 0:
                pnt_ls[i] = 4

            if tmp0 == 1 and tmp2 == 2:
                pnt_ls[i] = 5

        df_s1['Point'] = pnt_ls

        det_sec_ls = []
        for i in range(len(pnt_ls) - 1):
            tmp1 = pnt_ls[i]
            tmp2 = pnt_ls[i + 1]

            if tmp1 != tmp2:

                if (tmp2 == 0) | (tmp2 == 1):
                    det_sec_ls.append([i + 1, 2])

                if (tmp2 == 2) | (tmp2 == 3):
                    det_sec_ls.append([i + 1, 3])

                if (tmp2 == 4):
                    det_sec_ls.append([i, 2])

                if (tmp2 == 5):
                    det_sec_ls.append([i + 1, 3])

                if (tmp2 == 6) | (tmp2 == 7):
                    det_sec_ls.append([i + 1, 0])

                if (tmp2 == 8) | (tmp2 == 9):
                    det_sec_ls.append([i + 1, 1])

        det_sec_ls_ln = len(det_sec_ls)
        cnt = 0
        det_sec_clmn = []
        rng = 0
        df_len = len(df_s1)
        while cnt <= det_sec_ls_ln - 1:
            tmp = det_sec_ls[cnt]
            lngth = int(tmp[0])
            det = tmp[1]
            if cnt == (det_sec_ls_ln - 1):
                det_sec_clmn_ln = len(det_sec_clmn)
                x = df_len - det_sec_clmn_ln
                for i in range(x):
                    det_sec_clmn.append(det)

            else:
                if det_sec_ls_ln == 2:
                    x = lngth - rng + 1
                if det_sec_ls_ln > 2:
                    if cnt == 0:
                        x = lngth - rng + 1
                    else:
                        x = lngth - rng

                for i in range(x):
                    det_sec_clmn.append(det)

            cnt += 1
            rng = lngth

        df_s1['det_sect'] = det_sec_clmn
        risk_ls = []
        norm_ls = []
        for i in range(len(df_s1)):
            tmp = df_s1.iloc[i]['det_sect']

            if i < len(df_s1) - 1:
                tmp_n = df_s1.iloc[i + 1]['det_sect']
                if tmp == 0 & tmp_n != 0:
                    grp += 1
            grp = 0
            if tmp == 0:
                norm_ls.append(grp)
            if tmp != 0:
                norm_ls.append(-1)

        # Step 5
        df_s1['norm_group'] = norm_ls

        norm_grp = df_s1['norm_group'].unique().tolist()
        try:
            norm_grp.remove(-1)
        except:
            pass

        T_stab_ls = {}

        for i in norm_grp:
            mask = df_s1['norm_group'] == i
            tmp_df = df_s1[mask]
            tmp_T_stab = round(tmp_df['TAV'].mean(), 2)
            T_stab_ls[i] = tmp_T_stab

        for i in range(len(df_s1)):
            tmp = df_s1.iloc[i]['det_sect']

            if tmp == 0:

                grp = df_s1.iloc[i]['norm_group']

                T_stab = T_stab_ls[grp]
                tav = df_s1.iloc[i]['TAV']

                if T_stab - 50 > tav:
                    risk_ls.append("medium")
                if T_stab + 50 < tav:
                    risk_ls.append("medium")
                if T_stab - 50 <= tav <= T_stab + 50:
                    risk_ls.append("low")

            if tmp == 1:
                risk_ls.append("low")

            if tmp == 2:
                risk_ls.append("high")

            if tmp == 3:
                risk_ls.append("high")

        df_s1['risk'] = risk_ls

        plot_fig(df_s1, 2)

        return df_s1

    #Mixed
    # for i in [ 7, 8, 9]:

    # Normal
    # for i in [7]:
    for i in range(len(rt_ls)):

        rt_day = i
        sel_day = rt_ls[rt_day]
        mask = (df['Mon_day'] == sel_day)
        df_tmp = df.loc[mask]
        sel_day_bf = datetime.strptime(sel_day, '%m-%d')
        sel_month = sel_day_bf.month
        sel_days = sel_day_bf.day
        bf_day = sel_days - 1
        bf_day = str(bf_day)
        bf_date_str = str(sel_month) + '-' + str(bf_day)
        if sel_month < 10:
            bf_date_str = '0' + str(sel_month) + '-' + str(bf_day)

        # If the day before the day of interest exist in the dataset
        # Select the last entry from the previous day to the current day
        if bf_date_str in rt_ls:

            frst_ln = df_tmp.iloc[0]
            tmp = frst_ln['RT']
            df_loc = df.loc[df['RT'] == tmp]
            df_loc_num = df_loc.index[0]
            # adjust is to adjust to the date filtering from line 25
            # 18 is manually verified and adjusted.
            # Check the beginning row number of df
            adjust = 18
            bf_loc_num = df_loc_num - 1 - adjust
            df_bf = df.iloc[bf_loc_num]

            df_tmp.loc[df.index.max()+1] = df_bf
            df_tmp = df_tmp.sort_values(by=['RT_time'])
            # print()

        # Drop 'Mon_day', 'TFF', 'TEMP', and 'LST' column
        # End of step 1
        df_tmp = df_tmp.sort_values(by=['RT_time'])
        df_tmp['index_col'] = list(range(len(df_tmp)))
        ls_drp = ['Mon_day', 'TFF', 'TEMP', 'LST']
        df_s1 = df_tmp.drop(ls_drp, axis=1)


        # plt.figure
        # ax = df_s1.plot('index_col', 'TAV')
        # plt.title("TAV")
        # plt.xlabel("index")
        # plt.show()


        # Step 2
        try:
            df_s1['Diff_Temp'] = df_s1['TAV'].diff()
            df_s1['ABS'] = df_s1['Diff_Temp'].abs()
            df_s1 = df_s1.iloc[1:, :]
        except:
            continue
        # print()

        # Step 3
        # Calculate the ratio

        try:
            ABS_avg = df_s1['ABS'].mean()
            ABS_min_max = (min(df_s1['ABS']) + max(df_s1['ABS'])) / 2
            ratio = ABS_min_max / ABS_avg
            TAV_avg = df_s1['TAV'].mean()
        except:
            continue


        if np.isnan(ratio):
            # print("Data Corruption")
            continue
            # sys.exit(0)

        # Round ratio to the nearest 10th decimal
        ratio = round(ratio, 1)

        # Determine whether if the data contains warm-up/cool-down section
        # Based on the ratio value

        # Step 4-2
        if ratio <= 2:
            df_s1 = step_4_2(df_s1, TAV_avg)


        # Step 4-1
        if ratio > 2:
            df_s1 = step_4_1(df_s1, ABS_avg)


        df_s1 = df_s1[["CI", "RT", "CTT", "RT_time", "index_col", "det_sect", "risk"]]

        # # CTT extraction
        ctt_ls = df_s1['CTT'].to_list()
        ctt_total_cnt = []
        dict_ls = []

        for i in range(len(ctt_ls)):
            tmp = ctt_ls[i].replace('/', " ")
            tmp = tmp.split()
            # print()
            len_tmp = len(tmp)
            dict = {}
            for i in range(len_tmp):
                if (i % 2) == 0:
                    key = int(tmp[i])
                    val = int(tmp[i +1])
                    dict[key] = val
            ct_cnt = sum(map(int, dict.values()))
            ctt_total_cnt.append(ct_cnt)
            dict_ls.append(dict)
            ct_mal_cnt = []
            for i in dict_ls:
                abnorm_dict = {}
                if bool(i) is not False:
                    ct_mode = max(i, key=i.get)
                    ct_max = ct_mode * 1.10
                    ct_min = ct_mode - ct_mode * 0.10
                    for j in i.keys():
                        if j > ct_max or j < ct_min:
                            abnorm_dict[j] = i[j]
                    abnorm_cnt = sum(abnorm_dict.values())
                    if abnorm_cnt == 0:
                        ct_mal_cnt.append(None)
                    else:
                        ct_mal_cnt.append(abnorm_cnt)
                else:
                    ct_mode = None
                    ct_mal_cnt.append(ct_mode)



        df_s1['abnorm_ctt'] = ct_mal_cnt
        df_s1['shot_count'] = ctt_total_cnt
        df_s1['time'] = pd.to_datetime(df['RT_time']).dt.strftime("%H")
        df_s1['date'] = pd.to_datetime(df['RT_time']).dt.strftime("%Y%m%d")
        df_final = df_final.append(df_s1)

    df_final = df_final[["date", "time", "CI", "shot_count", "risk", "det_sect", "abnorm_ctt"]]
    df_final = df_final.sort_values(['date', 'time'], ascending=[True, True])
    result = df_final.to_json(orient="records")
    print(result)
    return json.loads(result)

