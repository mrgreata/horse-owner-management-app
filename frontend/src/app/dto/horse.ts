import { OwnerDto } from './owner';
import {Sex} from './sex';

export interface Horse {
  id?: number;
  name: string;
  description?: string;
  dateOfBirth: Date;
  sex: Sex;
  owner?: OwnerDto | null;

  // NEU für US4
  motherId?: number | null;
  fatherId?: number | null;
}

export interface HorseSearch {
  name?: string;
  description?: string;
  bornBefore?: Date;   // wird als yyyy-MM-dd gesendet
  sex?: Sex;
  ownerName?: string;  // Teilstring-Suche
  limit?: number;
}

export interface HorseCreate {
  name: string;
  description?: string;
  dateOfBirth: Date;
  sex: Sex;
  ownerId?: number | null;

  // NEU für US4
  motherId?: number | null;
  fatherId?: number | null;
}

export function convertFromHorseToCreate(horse: Horse): HorseCreate {
  return {
    name: horse.name,
    description: horse.description,
    dateOfBirth: horse.dateOfBirth,
    sex: horse.sex,
    ownerId: horse.owner?.id ?? null,
    motherId: horse.motherId ?? null,
    fatherId: horse.fatherId ?? null,
  };
}
