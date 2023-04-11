from composing import Song, Percussion, Instrument, Scale, Chord, note2number, number2note
import numpy as np
import os


def run():
    seed = np.random.randint(1000000)
    song_name = name_from_seed(seed)

    song = Song(120)

    # Very simple fixed drumset
    drum_track = song.drum_track()
    for i in range(0, 16, 4):
        print(i)
        drum_track.add_beat(Percussion.BASS_DRUM_1, i, 1)
        drum_track.add_beat(Percussion.SNARE_ACOUSTIC, i + 2, 1)
    
    piano_track = song.new_track(Instrument.PIANO_ACOUSTIC_BRIGHT)

    # Use two scales for the melody, so we have more range
    melody_range = Scale.MAJOR.start_from("C4") + Scale.MAJOR.start_from("C5")

    # # Generate melody by random walk in selected scale
    # current_note = len(melody_range) // 2
    # for i in range(16):
    #     piano_track.add_note(melody_range[current_note], i, 1)
    #     # Also, notes can be added at fractions of beats
    #     if np.random.uniform() < 0.33:
    #         piano_track.add_note(melody_range[current_note], i + 0.5, 1)

    #     current_note = current_note + np.random.randint(-2, 3)
    #     current_note = np.clip(current_note, 0, len(melody_range) - 1)

    # # Some examples on how to use chords and arpeggios
    # piano_track.add_arpeggio(
    #     Chord.MAJOR_7TH.start_from(np.random.choice(melody_range)),
    #     24, 4, 1.0
    # )
    piano_track.add_chord(
        Chord.MAJOR.start_from("C4"),
        0, 2
    )

    if not os.path.exists('out'):
        os.mkdir('out')

    song.save('out/{}.mid'.format(song_name))
    print('Generated song "{}"'.format(song_name))


def name_from_seed(seed):
    with open('assets/nouns.txt') as f:
        nouns = [s.strip() for s in f.readlines()]
    with open('assets/adjectives.txt') as f:
        adjectives = [s.strip() for s in f.readlines()]
    noun = nouns[seed % 1000]
    adjective = adjectives[seed // 1000]
    return '{}_{}'.format(adjective, noun)


if __name__ == '__main__':
    run()
