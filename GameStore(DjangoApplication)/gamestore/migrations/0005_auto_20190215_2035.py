# Generated by Django 2.1.5 on 2019-02-15 18:35

from django.conf import settings
from django.db import migrations, models
import django.db.models.deletion


class Migration(migrations.Migration):

    dependencies = [
        migrations.swappable_dependency(settings.AUTH_USER_MODEL),
        ('gamestore', '0004_auto_20190215_2028'),
    ]

    operations = [
        migrations.CreateModel(
            name='Purchase_log',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('name_of_the_game', models.TextField(max_length=140)),
                ('score', models.FloatField()),
                ('userid', models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, to=settings.AUTH_USER_MODEL)),
            ],
        ),
        migrations.AlterField(
            model_name='games',
            name='name_of_the_game',
            field=models.TextField(max_length=140),
        ),
    ]
