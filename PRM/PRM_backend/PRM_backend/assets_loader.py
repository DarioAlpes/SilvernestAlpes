# -*- coding: utf-8 -*
import os


def get_asset_path(asset_name):
    return os.path.join(os.path.dirname(os.path.realpath(__file__)), 'assets', asset_name)
