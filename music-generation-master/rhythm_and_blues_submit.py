from platform import platform
from threading import local
from composing import Song, Percussion, Instrument, Scale, Chord, note2number, number2note
import numpy as np
import os


def run():
    seed = np.random.randint(1000000)
    song_name = name_from_seed(seed)

    tempo = np.random.randint(110, 121)
    song = Song(tempo)

    # procedural drums
    # 4/4 classic rhythmic pattern
    drum_track = song.drum_track()

    for i in range(0, 30):
        # first and second beats
        for j in range(0, 8, 4):
            drum_track.add_beat(Percussion.BASS_DRUM_1, i * 16 + j, 0.5)
            drum_track.add_beat(Percussion.SNARE_ACOUSTIC, i * 16 + j + 1, 0.5)
            drum_track.add_beat(Percussion.BASS_DRUM_1, i * 16 + j + 1.5, 0.5)
            drum_track.add_beat(Percussion.SNARE_ACOUSTIC, i * 16 + j + 3, 0.5)

        # third beat
        drum_track.add_beat(Percussion.BASS_DRUM_1, i * 16 + 8, 0.5)
        drum_track.add_beat(Percussion.SNARE_ACOUSTIC, i * 16 + 8 + 1, 0.5)
        # number of notes of third part(from 1.5 to 3) will be uniformly distributed in {1,3,6}
        numberOfNotesOfThirdPart = np.random.randint(1,4)
        if (numberOfNotesOfThirdPart == 2):
            numberOfNotesOfThirdPart *= 3
        gapOfThirdPart = 1.5 / numberOfNotesOfThirdPart
        for j in range(0, numberOfNotesOfThirdPart):
            drum_track.add_beat(Percussion.BASS_DRUM_1, i * 16 + 8 + 1.5 + j * gapOfThirdPart, gapOfThirdPart)
        drum_track.add_beat(Percussion.SNARE_ACOUSTIC, i * 16 + 8 + 3.0, 0.5)

        # fourth beat
        drum_track.add_beat(Percussion.BASS_DRUM_1, i * 16 + 12, 0.5)
        drum_track.add_beat(Percussion.SNARE_ACOUSTIC, i * 16 + 12 + 1, 0.5)
        # Third part(from 1.5 to 2.75) will be have two sixteenth note(1/4 = 0.25 for each sixteenth note)
        twoStartingPointOfThirdPart = np.random.choice(5,2)
        drum_track.add_beat(Percussion.BASS_DRUM_1, i * 16 + 12 + 1.5 + twoStartingPointOfThirdPart[0] * 0.25, 0.25)
        drum_track.add_beat(Percussion.BASS_DRUM_1, i * 16 + 12 + 1.5 + twoStartingPointOfThirdPart[1] * 0.25, 0.25)
        drum_track.add_beat(Percussion.SNARE_ACOUSTIC, i * 16 + 12 + 3, 0.5)

    # melody(including procedural bass; melody only involves random walk under certain limitations)
    melody_range = Scale.MAJOR.start_from("C3") + Scale.MAJOR.start_from("C4") + Scale.MAJOR.start_from("C5") + Scale.MAJOR.start_from("C6")
    piano_track = song.new_track(Instrument.PIANO_HONKY_TONK)
    bass_track = song.new_track(Instrument.BASS_ACOUSTIC)
    print(len(melody_range))
    for i in range(0, 30):
        
        #first beat 
        # 3 increases, 1 decrease, and 1 increase
        firstBeat = []
        currentNote = 7
        piano_track.add_note(melody_range[currentNote], i * 16 + 0 + 0, 4.0)
        firstBeat.append(currentNote)
        currentNote += np.random.randint(3,5)
        currentNote = np.clip(currentNote, 0, len(melody_range) - 1)
        piano_track.add_note(melody_range[currentNote], i * 16 + 0 + 0.5, 3.5)
        firstBeat.append(currentNote)
        currentNote += np.random.randint(3,5)
        currentNote = np.clip(currentNote, 0, len(melody_range) - 1)
        piano_track.add_note(melody_range[currentNote], i * 16 + 0 + 1.0, 3.0)
        firstBeat.append(currentNote)
        currentNote += np.random.randint(1,3)
        currentNote = np.clip(currentNote, 0, len(melody_range) - 1)
        piano_track.add_note(melody_range[currentNote], i * 16 + 0 + 1.5, 2.5)
        firstBeat.append(currentNote)
        currentNote -= np.random.randint(1,3)
        currentNote = np.clip(currentNote, 0, len(melody_range) - 1)
        piano_track.add_note(melody_range[currentNote], i * 16 + 0 + 2.5, 2.0)
        firstBeat.append(currentNote)
        currentNote += np.random.randint(3,5)
        currentNote = np.clip(currentNote, 0, len(melody_range) - 1)
        piano_track.add_note(melody_range[currentNote], i * 16 + 0 + 3.0, 1.5)
        firstBeat.append(currentNote)

        # second beat
        # same as first beat except that only the first note decreases w.r.t the first beat
        secondBeat = firstBeat.copy()
        currentNote = 7 - np.random.randint(1,3)
        currentNote = np.clip(currentNote, 0, len(melody_range) - 1)
        piano_track.add_note(melody_range[currentNote], i * 16 + 4 + 0, 4.0)
        secondBeat[0] = currentNote
        piano_track.add_note(melody_range[firstBeat[1]], i * 16 + 4 + 0.5, 3.5)
        piano_track.add_note(melody_range[firstBeat[2]], i * 16 + 4 + 1.0, 3.0)
        piano_track.add_note(melody_range[firstBeat[3]], i * 16 + 4 + 1.5, 2.5)
        piano_track.add_note(melody_range[firstBeat[4]], i * 16 + 4 + 2.5, 2.0)
        piano_track.add_note(melody_range[firstBeat[5]], i * 16 + 4 + 3.0, 1.5)

        # third beat
        # shift the first beat up
        thirdBeat = []
        shiftUp = np.random.randint(3,5)
        currentNote = 7 + shiftUp
        currentNote = np.clip(currentNote, 0, len(melody_range) - 1)
        piano_track.add_note(melody_range[currentNote], i * 16 + 8 + 0, 4.0)
        thirdBeat.append(currentNote)
        currentNote = firstBeat[1] + shiftUp
        currentNote = np.clip(currentNote, 0, len(melody_range) - 1)
        piano_track.add_note(melody_range[currentNote], i * 16 + 8 + 0.5, 3.5)
        thirdBeat.append(currentNote)
        currentNote = firstBeat[2] + shiftUp
        currentNote = np.clip(currentNote, 0, len(melody_range) - 1)
        piano_track.add_note(melody_range[currentNote], i * 16 + 8 + 1.0, 3.0)
        thirdBeat.append(currentNote)
        currentNote = firstBeat[3] + shiftUp
        currentNote = np.clip(currentNote, 0, len(melody_range) - 1)
        piano_track.add_note(melody_range[currentNote], i * 16 + 8 + 1.5, 2.5)
        thirdBeat.append(currentNote)
        currentNote = firstBeat[4] + shiftUp
        currentNote = np.clip(currentNote, 0, len(melody_range) - 1)
        piano_track.add_note(melody_range[currentNote], i * 16 + 8 + 2.5, 2.0)
        thirdBeat.append(currentNote)
        currentNote = firstBeat[5] + shiftUp
        currentNote = np.clip(currentNote, 0, len(melody_range) - 1)
        highestNoteOfThirdBeat = currentNote
        piano_track.add_note(melody_range[currentNote], i * 16 + 8 + 3.0, 1.5)
        thirdBeat.append(currentNote)
        # one additional note than first and second beat
        currentNote -= 2
        currentNote = np.clip(currentNote, 0, len(melody_range) - 1)
        piano_track.add_note(melody_range[currentNote], i * 16 + 8 + 3.5, 1.5)
        thirdBeat.append(currentNote)

        # fourth beat
        # platform note will be added to fourthBeat only once
        fourthBeat = []
        platform = firstBeat[3]
        highestNoteOfFourthBeat = highestNoteOfThirdBeat - np.random.randint(1)
        currentNote = highestNoteOfFourthBeat
        currentNote = np.clip(currentNote, 0, len(melody_range) - 1)
        piano_track.add_note(melody_range[currentNote], i * 16 + 12 + 0, 4.0)
        fourthBeat.append(currentNote)
        currentNote = platform
        piano_track.add_note(melody_range[currentNote], i * 16 + 12 + 0.5, 1.0)
        fourthBeat.append(currentNote)
        currentNote = highestNoteOfFourthBeat - 1
        currentNote = np.clip(currentNote, 0, len(melody_range) - 1)
        piano_track.add_note(melody_range[currentNote], i * 16 + 12 + 1.0, 3.0)
        fourthBeat.append(currentNote)
        currentNote = platform
        piano_track.add_note(melody_range[currentNote], i * 16 + 12 + 1.5, 1.5)
        currentNote = highestNoteOfFourthBeat - 2
        currentNote = np.clip(currentNote, 0, len(melody_range) - 1)
        piano_track.add_note(melody_range[currentNote], i * 16 + 12 + 2.0, 2.0)
        fourthBeat.append(currentNote)
        currentNote = platform
        piano_track.add_note(melody_range[currentNote], i * 16 + 12 + 3.0, 1.0)


        # procedural bass
        # choose 2 notes from each melody beat and use it as bass that last in the whole beat
        for j in range(0, 16, 4):
            if (j / 4 == 0):
                twoBassNotes = np.random.choice(firstBeat, 2)
                bass_track.add_note(melody_range[twoBassNotes[0]], i * 16 + j, 4.0)
                bass_track.add_note(melody_range[twoBassNotes[1]], i * 16 + j, 4.0)
            elif (j / 4 == 1):
                twoBassNotes = np.random.choice(secondBeat, 2)
                bass_track.add_note(melody_range[twoBassNotes[0]], i * 16 + j, 4.0)
                bass_track.add_note(melody_range[twoBassNotes[1]], i * 16 + j, 4.0)
            elif (j / 4 == 2):
                twoBassNotes = np.random.choice(thirdBeat, 2)
                bass_track.add_note(melody_range[twoBassNotes[0]], i * 16 + j, 4.0)
                bass_track.add_note(melody_range[twoBassNotes[1]], i * 16 + j, 4.0)
            elif (j / 4 == 3):
                twoBassNotes = np.random.choice(fourthBeat, 2)
                bass_track.add_note(melody_range[twoBassNotes[0]], i * 16 + j, 4.0)
                bass_track.add_note(melody_range[twoBassNotes[1]], i * 16 + j, 4.0)

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
