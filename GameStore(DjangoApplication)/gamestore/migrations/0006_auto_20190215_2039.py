# Generated by Django 2.1.5 on 2019-02-15 18:39

from django.db import migrations


class Migration(migrations.Migration):

    dependencies = [
        ('gamestore', '0005_auto_20190215_2035'),
    ]

    operations = [
        migrations.RenameField(
            model_name='purchase_log',
            old_name='score',
            new_name='price',
        ),
    ]