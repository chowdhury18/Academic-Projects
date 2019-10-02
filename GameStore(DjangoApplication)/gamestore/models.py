# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models


# Create your models here.
'''
class User(models.Model):
    name   = models.TextField()
    role   = models.PositiveSmallIntegerField()
    email  = models.EmailField()
'''

class Games(models.Model):
    name_of_the_game = models.TextField(max_length=140)
    url_of_the_game = models.TextField()
    price  = models.FloatField()
    date_added = models.DateField()
    time_added = models.TimeField()
    description = models.TextField()
    category = models.TextField()
    #userid = models.ForeignKey(User, on_delete=models.CASCADE, related_name='games_userid')
    userid = models.ForeignKey(
        'users.CustomUser',
        on_delete=models.CASCADE
    )

class Game_log(models.Model):
    status = models.PositiveSmallIntegerField()
    save_state = models.TextField()
    #userid = models.ForeignKey(User, on_delete=models.CASCADE, related_name='gamelog_userid')
    userid = models.ForeignKey(
        'users.CustomUser',
        on_delete=models.CASCADE
    )
    gameid = models.ForeignKey(Games, on_delete=models.CASCADE, related_name='gamelog_gameid')

class Game_Details(models.Model):
    score = models.FloatField()
    date = models.DateField()
    time = models.TimeField()
    #userid = models.ForeignKey(User, on_delete=models.CASCADE, related_name='gamedetails_userid')
    userid = models.ForeignKey(
        'users.CustomUser',
        on_delete=models.CASCADE
    )
    gameid = models.ForeignKey(Games, on_delete=models.CASCADE, related_name='gamedetails_gameid')

class Admin_log(models.Model):
    date_modified = models.DateField()
    time_modified = models.TimeField()
    price_updated  = models.FloatField()
    description_modified = models.TextField()
    category_modified = models.TextField()
    url_updated = models.TextField()
    #userid = models.ForeignKey(User, on_delete=models.CASCADE, related_name='adminlog_userid')
    userid = models.ForeignKey(
        'users.CustomUser',
        on_delete=models.CASCADE
    )
    gameid = models.ForeignKey(Games, on_delete=models.CASCADE, related_name='adminlog_gameid')

class Purchase(models.Model):
    date_purchased = models.DateField()
    time_purchased = models.TimeField()
    price_purchased  = models.FloatField()
    #userid = models.ForeignKey(User, on_delete=models.CASCADE, related_name='purchase_userid')
    userid = models.ForeignKey(
        'users.CustomUser',
        on_delete=models.CASCADE
    )
    gameid = models.ForeignKey(Games, on_delete=models.CASCADE, related_name='purchase_gameid')

    class Meta:
        unique_together = (('userid', 'gameid'),)


class Purchase_log(models.Model):
    name_of_the_game = models.TextField(max_length=140)
    price = models.FloatField()
    userid = models.ForeignKey(
        'users.CustomUser',
        on_delete=models.CASCADE
    )
