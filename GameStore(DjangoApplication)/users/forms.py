from django import forms
from django.contrib.auth.forms import UserCreationForm, UserChangeForm
from .models import CustomUser

# class CustomUserCreationForm(UserCreationForm):
#     email = forms.EmailField(max_length=200, help_text='Required')
#     #color = forms.CharField(max_length=30, required=False, help_text='Optional.')
#     class Meta(UserCreationForm):
#         model = CustomUser
#         fields = ('username', 'email', 'role')
#
class CustomUserChangeForm(UserChangeForm):
    email = forms.EmailField(max_length=200, help_text='Required')
    class Meta:
        model = CustomUser
        fields = ('username', 'email', 'role')
        #fields = UserChangeForm.Meta.fields

class SignupForm(UserCreationForm):
    email = forms.EmailField(max_length=200, help_text='Required')
    class Meta:
        model = CustomUser
        fields = ('username', 'email', 'role', 'password1', 'password2')
