from django.db import models

from django.contrib.auth.models import AbstractUser, UserManager



class CustomUserManager(UserManager):
    pass

class CustomUser(AbstractUser):
    #description = models.TextField(default='test')
    MY_CHOICES = (
        ('1', 'Developer'),
        ('2', 'Player'),
    )
    role =  models.CharField(max_length=1, choices=MY_CHOICES, default='2')
    #objects = CustomUserManager()
