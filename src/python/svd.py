#!/usr/bin/env python3
import numpy as np
from math import sqrt
from utils import dissimilarity, special_sort


def create_svd_matrix(temp_matrix, user_list, item_list, number_of_features):
    users = list(user_list)
    # items = list(item_list)

    user_index = {users[i]: i for i in range(len(users))}
    # item_index = {items[i]: i for i in range(len(items))}

    mask = np.isnan(temp_matrix)
    masked_arr = np.ma.masked_array(temp_matrix, mask)

    prediction_mask = ~mask

    item_means = np.mean(masked_arr, axis=0)

    item_means_tiled = np.tile(item_means, (temp_matrix.shape[0], 1))

    # user_means = np.mean(masked_arr, axis=1)

    utility_matrix = masked_arr.filled(item_means)

    utility_matrix = utility_matrix - item_means_tiled

    U, S, V = np.linalg.svd(utility_matrix, full_matrices=False)

    U = U[:, 0:number_of_features]
    V = V[0:number_of_features, :]
    s_root = np.diag([sqrt(S[i]) for i in range(0, number_of_features)])

    Usk = np.dot(U, s_root)
    skV = np.dot(s_root, V)
    UsV = np.dot(Usk, skV)

    return Usk, UsV + item_means_tiled, user_index, prediction_mask


def topN_similar(user_query, Usk, user_index, N=10, weight=True):

    out = list()
    if user_query not in user_index:
        raise Exception("Invalid user")
    else:
        for user in user_index:
            if user != user_query:
                sim = dissimilarity(
                    Usk[user_index[user], :],
                    Usk[user_index[user_query], :],
                    weighted=weight,
                )
                out.append((user, sim))
    out = special_sort(out, order="ascending")
    out = out[:N]
    return out


def recommend(predMask, UsV, user_index, users_list, items, N=10, values=False):
    # utilMat element not zero means that element has already been
    # discovered by the user and can not be recommended
    predMat = np.ma.masked_where(predMask, UsV).filled(fill_value=-999)
    out = []

    if values == True:
        for user in users_list:
            try:
                j = user_index[user]
            except:
                raise Exception("Invalid user:", user)
            max_indices = predMat[j, :].argsort()[-N:][::-1]
            out.append([(items[index], predMat[j, index]) for index in max_indices])

    else:
        for user in users_list:
            try:
                j = user_index[user]
            except:
                raise Exception("Invalid user:", user)
            max_indices = predMat[j, :].argsort()[-N:][::-1]
            out.append([items[index] for index in max_indices])

    return out
