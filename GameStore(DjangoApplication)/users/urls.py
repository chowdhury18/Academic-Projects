from django.urls import path
from users import views
from django.contrib.auth import views as auth_views
from django.conf.urls import url

path('accounts/login/', auth_views.LoginView.as_view()),


urlpatterns = [
    url(r'^signup/$', views.signup, name='signup'),
    url(r'^activate/(?P<uidb64>[0-9A-Za-z_\-]+)/(?P<token>[0-9A-Za-z]{1,13}-[0-9A-Za-z]{1,20})/$',
        views.activate, name='activate'),
]
