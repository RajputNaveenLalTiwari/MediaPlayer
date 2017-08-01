package com.example.mediaplayer;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener,View.OnClickListener
{
    private MediaPlayer mediaPlayer;
    private List<AudioModel> audioModels;
    private int current_index;
    private boolean isPlaying = true;
    private Button previous_audio,play_pause_audio,next_audio;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previous_audio = (Button) findViewById(R.id.previous);
        play_pause_audio = (Button) findViewById(R.id.play_pause);
        next_audio = (Button) findViewById(R.id.next);
        previous_audio.setOnClickListener(this);
        play_pause_audio.setOnClickListener(this);
        next_audio.setOnClickListener(this);

        mediaPlayer = new MediaPlayer();
        readAllAudioFiles();

        if ((audioModels !=null) && (!audioModels.isEmpty()))
        {
            current_index = 0;
            long audio_id = audioModels.get(current_index).audio_id;
            playAudio(audio_id);
        }
    }

    private void readAllAudioFiles()
    {
        audioModels = new ArrayList<>();
        ContentResolver contentResolver = getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String columns[] = null;
        String whereClause = null;
        String whereArgs[] = null;
        String sortingOrder = null;

        Cursor cursor = contentResolver.query(uri,columns,whereClause,whereArgs,sortingOrder);

        if (cursor != null && cursor.moveToFirst())
        {
            do
            {
                long audio_id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                String audio_title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));

                AudioModel audioModel = new AudioModel();
                audioModel.audio_id = audio_id;
                audioModel.audio_title = audio_title;

                audioModels.add(audioModel);
            }while (cursor.moveToNext());

            cursor.close();
        }
    }

    private void playAudio(long audio_id)
    {

        if (mediaPlayer != null)
        {
            releaseMediaPlayer();
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnCompletionListener(this);
            Uri contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,audio_id);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try
            {
                mediaPlayer.setDataSource(getApplicationContext(),contentUri);
                mediaPlayer.prepare();
                mediaPlayer.start();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

        }
    }

    private void releaseMediaPlayer()
    {
        if (mediaPlayer != null)
        {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void playAudio()
    {
        if (mediaPlayer!=null)
        {
            mediaPlayer.seekTo(mediaPlayer.getCurrentPosition());
            mediaPlayer.start();
        }
    }

    private void pauseAudio()
    {
        if (mediaPlayer!=null)
        {
            mediaPlayer.pause();
        }
    }


    @Override
    public void onCompletion(MediaPlayer mp)
    {
        if ((current_index+1) < audioModels.size())
        {
            current_index = current_index+1;
            long audio_id = audioModels.get(current_index).audio_id;
            playAudio(audio_id);
        }
        else if ((current_index-1) != -1)
        {
            current_index = current_index-1;
            long audio_id = audioModels.get(current_index).audio_id;
            playAudio(audio_id);
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        releaseMediaPlayer();
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.next:
                if ((current_index+1) < audioModels.size())
                {
                    current_index = current_index+1;
                    long audio_id = audioModels.get(current_index).audio_id;
                    playAudio(audio_id);
                }
                break;
            case R.id.previous:
                if ((current_index-1) != -1)
                {
                    current_index = current_index - 1;
                    long audio_id = audioModels.get(current_index).audio_id;
                    playAudio(audio_id);
                }
                break;
            case R.id.play_pause:
                if (isPlaying)
                {
                    pauseAudio();
                    play_pause_audio.setText("Pause");
                }
                else
                {
                    playAudio();
                    play_pause_audio.setText("Play");
                }

                isPlaying = !isPlaying;
                break;
        }
    }
}
