package ru.akbashev.instacollage;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;


public class SendDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        Bundle args = getArguments();

        byte[] byteArray = args.getByteArray("pathOfBmp");
        final Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_dialog, null);
        ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
        imageView.setImageBitmap(bmp);
        builder.setView(view)
                .setPositiveButton("Отправить", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String pathOfBmp = MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), bmp, "InstaCollage", null);
                        Uri bmpUri = Uri.parse(pathOfBmp);
                        Intent emailIntent = new Intent(     android.content.Intent.ACTION_SEND);
                        emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        emailIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
                        emailIntent.setType("image/png");
                        startActivity(emailIntent);
                    }
                })
                .setNegativeButton("Вернуться", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        return builder.create();
    }
}
